from __future__ import annotations

import argparse
import shutil
import struct
import subprocess
from pathlib import Path

PAGE_SIZE = 512
RANGES = (
    (0x2E80, 0x33FF),
    (0x3400, 0x4DBF),
    (0x4E00, 0x9FFF),
    (0xF900, 0xFAFF),
    (0xFE30, 0xFE4F),
    (0xFF00, 0xFFEF),
    (0x20000, 0x2A6DF),
    (0x2A700, 0x2B73F),
    (0x2B740, 0x2B81F),
    (0x2B820, 0x2CEAF),
    (0x2CEB0, 0x2EBEF),
    (0x2F800, 0x2FA1F),
    (0x30000, 0x3134F),
    (0x31350, 0x323AF),
)


def table(data: bytes, tag: str) -> bytes:
    count = struct.unpack_from(">H", data, 4)[0]
    offset = 12
    for _ in range(count):
        table_tag = data[offset:offset + 4].decode("latin1")
        _, table_offset, table_length = struct.unpack_from(">III", data, offset + 4)
        if table_tag == tag:
            return data[table_offset:table_offset + table_length]
        offset += 16
    raise ValueError(f"Missing {tag} table")


def cmap_codepoints(font_file: Path) -> set[int]:
    cmap = table(font_file.read_bytes(), "cmap")
    subtable_count = struct.unpack_from(">H", cmap, 2)[0]
    codepoints: set[int] = set()

    for index in range(subtable_count):
        subtable_offset = struct.unpack_from(">I", cmap, 8 + index * 8)[0]
        fmt = struct.unpack_from(">H", cmap, subtable_offset)[0]

        if fmt == 4:
            seg_count = struct.unpack_from(">H", cmap, subtable_offset + 6)[0] // 2
            end_offset = subtable_offset + 14
            start_offset = end_offset + seg_count * 2 + 2
            ends = struct.unpack_from(">" + "H" * seg_count, cmap, end_offset)
            starts = struct.unpack_from(">" + "H" * seg_count, cmap, start_offset)
            for start, end in zip(starts, ends):
                if start == 0xFFFF and end == 0xFFFF:
                    continue
                codepoints.update(range(start, end + 1))

        if fmt == 12:
            group_count = struct.unpack_from(">I", cmap, subtable_offset + 12)[0]
            group_offset = subtable_offset + 16
            for group in range(group_count):
                start, end, _ = struct.unpack_from(">III", cmap, group_offset + group * 12)
                codepoints.update(range(start, end + 1))

    return codepoints


def page_name(start: int, end: int) -> str:
    return f"misans_cjk_{start:04x}_{end:04x}"


def pages(codepoints: set[int]) -> list[tuple[str, int, int, list[int]]]:
    result: list[tuple[str, int, int, list[int]]] = []
    for range_start, range_end in RANGES:
        page_start = range_start
        while page_start <= range_end:
            page_end = min(page_start + PAGE_SIZE - 1, range_end)
            chars = [cp for cp in range(page_start, page_end + 1) if cp in codepoints]
            if chars:
                result.append((page_name(page_start, page_end), page_start, page_end, chars))
            page_start = page_end + 1
    return result


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--size", type=int, default=64)
    parser.add_argument("--pxrange", type=int, default=12)
    parser.add_argument("--padding", type=int, default=12)
    parser.add_argument("--force", action="store_true")
    args = parser.parse_args()

    script_dir = Path(__file__).resolve().parent
    project_dir = script_dir.parent.parent
    exe = script_dir / "msdf-atlas-gen" / "msdf-atlas-gen.exe"
    font_file = project_dir / "forge" / "src" / "main" / "resources" / "assets" / "multiloaderclient" / "fonts" / "misans_regular.ttf"
    output_dir = font_file.parent
    charset_dir = script_dir / "misans_cjk_charsets"

    if not exe.exists():
        raise FileNotFoundError(exe)
    if not font_file.exists():
        raise FileNotFoundError(font_file)

    charset_dir.mkdir(parents=True, exist_ok=True)
    font_codepoints = cmap_codepoints(font_file)
    cjk_pages = pages(font_codepoints)
    print(f"MiSans cmap codepoints: {len(font_codepoints)}")
    print(f"CJK atlas pages with glyphs: {len(cjk_pages)}")

    for index, (name, start, end, chars) in enumerate(cjk_pages, start=1):
        charset_file = charset_dir / f"{name}.txt"
        image_file = output_dir / f"{name}.png"
        json_file = output_dir / f"{name}.json"

        charset_file.write_text("".join(f"0x{cp:X}\n" for cp in chars), encoding="ascii")
        if not args.force and image_file.exists() and json_file.exists():
            print(f"[{index}/{len(cjk_pages)}] skip {name} ({len(chars)} glyphs)")
            continue

        tmp_image = image_file.with_suffix(".png.tmp")
        tmp_json = json_file.with_suffix(".json.tmp")
        command = [
            str(exe),
            "-font", str(font_file),
            "-charset", str(charset_file),
            "-type", "msdf",
            "-format", "png",
            "-size", str(args.size),
            "-pxrange", str(args.pxrange),
            "-pxpadding", str(args.padding),
            "-square2",
            "-yorigin", "top",
            "-overlap",
            "-scanline",
            "-imageout", str(tmp_image),
            "-json", str(tmp_json),
            "-threads", "0",
        ]

        print(f"[{index}/{len(cjk_pages)}] generate {name} U+{start:04X}-U+{end:04X} ({len(chars)} glyphs)")
        subprocess.run(command, check=True)
        shutil.move(str(tmp_image), image_file)
        shutil.move(str(tmp_json), json_file)


if __name__ == "__main__":
    main()
