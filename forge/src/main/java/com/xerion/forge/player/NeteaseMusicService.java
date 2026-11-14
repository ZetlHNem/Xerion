package com.xerion.forge.player;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.FactoryRegistry;
import javazoom.jl.player.advanced.AdvancedPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

public final class NeteaseMusicService {
    private static final int FEATURED_PLAYLIST_PAGE_SIZE = 50;
    private static final int FEATURED_PLAYLIST_PAGE_COUNT = 14;
    private static final String SEARCH_ENDPOINT = "https://music.163.com/api/search/get/web";
    private static final String FEATURED_PLAYLIST_ENDPOINT =
            "https://music.163.com/api/playlist/list?cat=%E5%85%A8%E9%83%A8&order=hot&limit="
                    + FEATURED_PLAYLIST_PAGE_SIZE + "&offset=";
    private static final String PLAYLIST_DETAIL_ENDPOINT =
            "https://music.163.com/api/v6/playlist/detail?n=1000&s=8&id=";
    private static final String URL_ENDPOINT = "https://interface3.music.163.com/api/song/enhance/player/url/v1";
    private static final String LYRIC_ENDPOINT = "https://music.163.com/api/song/lyric?id=";
    private static final long LYRIC_SYNC_DELAY_MS = 200L;
    private static final int SEARCH_PAGE_SIZE = 50;
    private static final Pattern LYRIC_TIMESTAMP = Pattern.compile(
            "\\[(\\d{1,3}):(\\d{2})(?:\\.(\\d{1,3}))?\\](.*)");
    private static final Field JAVA_SOUND_SOURCE_FIELD = findJavaSoundSourceField();
    private static final Field ADVANCED_PLAYER_BITSTREAM_FIELD = findAdvancedPlayerBitstreamField();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();
    private static final ExecutorService METADATA_EXECUTOR = Executors.newFixedThreadPool(4,
            threadFactory("Xerion-NeteaseMetadata", Thread.NORM_PRIORITY - 1));
    private static final ExecutorService COVER_EXECUTOR = Executors.newFixedThreadPool(2,
            threadFactory("Xerion-NeteaseCover", Thread.MIN_PRIORITY));
    private static final ExecutorService AUDIO_EXECUTOR = Executors.newSingleThreadExecutor(
            threadFactory("Xerion-NeteaseAudio", Thread.NORM_PRIORITY));
    private static final Map<Long, ResourceLocation> COVER_TEXTURES = new ConcurrentHashMap<>();
    private static final Set<Long> COVER_LOADING = ConcurrentHashMap.newKeySet();
    private static final Map<Long, List<LyricLine>> LYRICS = new ConcurrentHashMap<>();
    private static final Map<Long, String> LYRIC_MESSAGES = new ConcurrentHashMap<>();
    private static final Set<Long> LYRIC_LOADING = ConcurrentHashMap.newKeySet();

    private static volatile List<Song> searchResults = List.of();
    private static volatile List<Playlist> playlistSearchResults = List.of();
    private static volatile List<UserProfile> userSearchResults = List.of();
    private static volatile List<Playlist> featuredPlaylists = List.of();
    private static volatile boolean featuredPlaylistsLoading;
    private static volatile String featuredPlaylistsStatus = "Loading playlists...";
    private static volatile long featuredPlaylistDay = currentEpochDay();
    private static volatile int featuredPlaylistOffset = featuredOffsetForDay(featuredPlaylistDay);
    private static volatile long featuredPlaylistGeneration;
    private static volatile String resultTitle = "Songs";
    private static volatile String searchQuery = "";
    private static volatile int searchOffset;
    private static volatile int searchTotal;
    private static volatile boolean searchLoading;
    private static volatile boolean searchHasMore;
    private static volatile int playlistSearchOffset;
    private static volatile int playlistSearchTotal;
    private static volatile boolean playlistSearchLoading;
    private static volatile boolean playlistSearchHasMore;
    private static volatile int userSearchOffset;
    private static volatile int userSearchTotal;
    private static volatile boolean userSearchLoading;
    private static volatile boolean userSearchHasMore;
    private static volatile String songSearchStatus = "Ready";
    private static volatile String playlistSearchStatus = "Ready";
    private static volatile String userSearchStatus = "Ready";
    private static volatile SearchCategory searchCategory = SearchCategory.SONGS;
    private static final Deque<BrowseState> NAVIGATION_HISTORY = new ArrayDeque<>();
    private static volatile long searchGeneration;
    private static volatile Song currentSong;
    private static volatile String status = "Ready";
    private static volatile boolean playing;
    private static volatile boolean paused;
    private static volatile long startedAt;
    private static volatile long elapsedBeforeStartMs;
    private static volatile TrackingPlayer player;
    private static volatile Future<?> playbackTask;
    private static long playbackGeneration;
    private static int currentIndex = -1;
    private static volatile PlayMode playMode = PlayMode.ORDER;
    private static final float VOLUME_LERP_PER_FRAME_AT_60_FPS = 0.16f;
    private static final double MAX_VOLUME_LERP_DELTA_SECONDS = 0.1D;
    private static volatile float volume = 1.0f;
    private static long lyricCursorSongId = -1L;
    private static int lyricCursorIndex = -1;
    private static long lyricCursorPositionMs = -1L;
    private static volatile boolean lyricSeekPending;
    private static volatile long lyricTimelineRevision;
    private static int resumeFrame;
    private static String currentAudioUrl;
    private static boolean currentAudioPreview;

    private NeteaseMusicService() {
    }

    private static ThreadFactory threadFactory(String prefix, int priority) {
        AtomicInteger sequence = new AtomicInteger();
        return runnable -> {
            Thread thread = new Thread(runnable, prefix + "-" + sequence.incrementAndGet());
            thread.setDaemon(true);
            thread.setPriority(priority);
            return thread;
        };
    }

    public static void requestFeaturedPlaylists() {
        long generation;
        int offset;
        synchronized (NeteaseMusicService.class) {
            long today = currentEpochDay();
            if (featuredPlaylistDay != today) {
                featuredPlaylistDay = today;
                featuredPlaylistOffset = featuredOffsetForDay(today);
                featuredPlaylists = List.of();
                featuredPlaylistsLoading = false;
                featuredPlaylistGeneration++;
            }
            if (featuredPlaylistsLoading || !featuredPlaylists.isEmpty()) {
                return;
            }
            featuredPlaylistsLoading = true;
            featuredPlaylistsStatus = "Loading playlists...";
            generation = ++featuredPlaylistGeneration;
            offset = featuredPlaylistOffset;
        }
        METADATA_EXECUTOR.submit(() -> loadFeaturedPlaylists(offset, generation));
    }

    public static void refreshFeaturedPlaylists() {
        long generation;
        int offset;
        synchronized (NeteaseMusicService.class) {
            featuredPlaylistDay = currentEpochDay();
            int poolSize = FEATURED_PLAYLIST_PAGE_SIZE * FEATURED_PLAYLIST_PAGE_COUNT;
            featuredPlaylistOffset = (featuredPlaylistOffset + FEATURED_PLAYLIST_PAGE_SIZE) % poolSize;
            offset = featuredPlaylistOffset;
            featuredPlaylistsLoading = true;
            featuredPlaylistsStatus = "Refreshing playlists...";
            generation = ++featuredPlaylistGeneration;
        }
        METADATA_EXECUTOR.submit(() -> loadFeaturedPlaylists(offset, generation));
    }

    private static long currentEpochDay() {
        return LocalDate.now().toEpochDay();
    }

    private static int featuredOffsetForDay(long epochDay) {
        return Math.floorMod(epochDay, FEATURED_PLAYLIST_PAGE_COUNT) * FEATURED_PLAYLIST_PAGE_SIZE;
    }

    private static void loadFeaturedPlaylists(int offset, long generation) {
        try {
            JsonObject root = JsonParser.parseString(get(FEATURED_PLAYLIST_ENDPOINT + offset)).getAsJsonObject();
            JsonArray values = root.getAsJsonArray("playlists");
            List<Playlist> playlists = new ArrayList<>();
            if (values != null) {
                for (JsonElement element : values) {
                    JsonObject value = element.getAsJsonObject();
                    playlists.add(new Playlist(
                            number(value, "id", 0L),
                            text(value, "name"),
                            text(value, "coverImgUrl"),
                            (int) number(value, "trackCount", 0L),
                            number(value, "playCount", 0L)));
                }
            }
            if (generation == featuredPlaylistGeneration) {
                featuredPlaylists = List.copyOf(playlists);
                featuredPlaylistsStatus = playlists.isEmpty() ? "No playlists" : "Ready";
            }
        } catch (Exception exception) {
            if (generation == featuredPlaylistGeneration) {
                featuredPlaylistsStatus = "Playlist loading failed";
            }
        } finally {
            if (generation == featuredPlaylistGeneration) {
                featuredPlaylistsLoading = false;
            }
        }
    }

    public static List<Playlist> featuredPlaylists() {
        return featuredPlaylists;
    }

    public static String featuredPlaylistsStatus() {
        return featuredPlaylistsStatus;
    }

    public static String resultTitle() {
        return resultTitle;
    }

    public static void openPlaylist(Playlist playlist) {
        openPlaylist(playlist, true);
    }

    public static void openPlaylist(Playlist playlist, boolean rememberCurrent) {
        if (playlist == null || playlist.id() <= 0L) {
            return;
        }
        long generation;
        synchronized (NeteaseMusicService.class) {
            if (rememberCurrent) {
                pushNavigationState();
            }
            searchGeneration++;
            generation = searchGeneration;
            searchQuery = "";
            searchOffset = 0;
            searchTotal = 0;
            searchHasMore = false;
            searchLoading = true;
            searchResults = List.of();
            searchCategory = SearchCategory.SONGS;
            currentIndex = -1;
            resultTitle = playlist.name();
            status = "Loading playlist...";
            songSearchStatus = status;
        }
        METADATA_EXECUTOR.submit(() -> loadPlaylist(playlist, generation));
    }

    public static void openUser(UserProfile user) {
        if (user == null || user.id() <= 0L) {
            return;
        }
        long generation;
        synchronized (NeteaseMusicService.class) {
            pushNavigationState();
            generation = ++searchGeneration;
            searchQuery = "";
            searchResults = List.of();
            playlistSearchResults = List.of();
            userSearchResults = List.of();
            searchOffset = 0;
            searchTotal = 0;
            searchHasMore = false;
            playlistSearchOffset = 0;
            playlistSearchTotal = 0;
            playlistSearchHasMore = false;
            playlistSearchLoading = true;
            searchCategory = SearchCategory.PLAYLISTS;
            currentIndex = -1;
            resultTitle = user.nickname();
            playlistSearchStatus = "Loading user playlists...";
            status = playlistSearchStatus;
        }
        METADATA_EXECUTOR.submit(() -> loadUserPlaylists(user, generation));
    }

    private static void loadUserPlaylists(UserProfile user, long generation) {
        try {
            String url = "https://music.163.com/api/user/playlist/?uid=" + user.id()
                    + "&limit=1000&offset=0";
            JsonObject root = JsonParser.parseString(get(url)).getAsJsonObject();
            JsonArray values = root.getAsJsonArray("playlist");
            List<Playlist> playlists = new ArrayList<>();
            if (values != null) {
                for (JsonElement element : values) {
                    playlists.add(playlistFromJson(element.getAsJsonObject()));
                }
            }
            synchronized (NeteaseMusicService.class) {
                if (generation != searchGeneration) {
                    return;
                }
                playlistSearchResults = List.copyOf(playlists);
                playlistSearchOffset = playlists.size();
                playlistSearchTotal = playlists.size();
                playlistSearchStatus = playlists.isEmpty()
                        ? "No public playlists" : playlists.size() + " playlists";
                status = playlistSearchStatus;
            }
        } catch (Exception exception) {
            if (generation == searchGeneration) {
                playlistSearchResults = List.of();
                playlistSearchStatus = "User playlists failed";
                status = playlistSearchStatus;
            }
        } finally {
            if (generation == searchGeneration) {
                playlistSearchLoading = false;
            }
        }
    }

    private static void loadPlaylist(Playlist playlist, long generation) {
        try {
            JsonObject root = JsonParser.parseString(get(PLAYLIST_DETAIL_ENDPOINT + playlist.id()))
                    .getAsJsonObject();
            JsonObject result = root.getAsJsonObject("playlist");
            if (result == null) {
                result = root.getAsJsonObject("result");
            }
            JsonArray tracks = result == null ? null : result.getAsJsonArray("tracks");
            JsonArray trackIds = result == null ? null : result.getAsJsonArray("trackIds");
            List<Long> orderedIds = new ArrayList<>();
            if (trackIds != null) {
                for (JsonElement element : trackIds) {
                    long id = number(element.getAsJsonObject(), "id", 0L);
                    if (id > 0L) {
                        orderedIds.add(id);
                    }
                }
            }
            List<Song> songs = orderedIds.isEmpty() ? new ArrayList<>() : loadSongsByIds(orderedIds);
            if (songs.isEmpty() && tracks != null) {
                for (JsonElement element : tracks) {
                    songs.add(songFromJson(element.getAsJsonObject()));
                }
            }
            synchronized (NeteaseMusicService.class) {
                if (generation != searchGeneration) {
                    return;
                }
                searchResults = List.copyOf(songs);
                searchOffset = songs.size();
                searchTotal = songs.size();
                status = songs.isEmpty() ? "No songs" : songs.size() + " songs";
                songSearchStatus = status;
            }
        } catch (Exception exception) {
            if (generation == searchGeneration) {
                searchResults = List.of();
                status = "Playlist loading failed";
                songSearchStatus = status;
            }
        } finally {
            if (generation == searchGeneration) {
                searchLoading = false;
            }
        }
    }

    public static void search(String query) {
        search(query, true);
    }

    public static void search(String query, boolean rememberCurrent) {
        String normalized = query == null ? "" : query.trim();
        if (normalized.isEmpty()) {
            status = "Enter a song or artist";
            return;
        }
        synchronized (NeteaseMusicService.class) {
            if (rememberCurrent) {
                pushNavigationState();
            }
            searchQuery = normalized;
            searchOffset = 0;
            searchTotal = 0;
            searchHasMore = true;
            searchLoading = true;
            searchResults = List.of();
            playlistSearchOffset = 0;
            playlistSearchTotal = 0;
            playlistSearchHasMore = true;
            playlistSearchLoading = true;
            playlistSearchResults = List.of();
            userSearchOffset = 0;
            userSearchTotal = 0;
            userSearchHasMore = true;
            userSearchLoading = true;
            userSearchResults = List.of();
            songSearchStatus = "Searching...";
            playlistSearchStatus = "Searching...";
            userSearchStatus = "Searching...";
            searchCategory = SearchCategory.SONGS;
            currentIndex = -1;
            resultTitle = "Search: " + normalized;
            searchGeneration++;
        }
        status = "Searching...";
        long generation = searchGeneration;
        METADATA_EXECUTOR.submit(() -> loadSearchPage(normalized, 0, generation));
        METADATA_EXECUTOR.submit(() -> loadPlaylistSearchPage(normalized, 0, generation));
        METADATA_EXECUTOR.submit(() -> loadUserSearchPage(normalized, 0, generation));
    }

    public static void loadMoreSearchResults() {
        String query = searchQuery;
        long generation = searchGeneration;
        if (query.isBlank()) {
            return;
        }
        switch (searchCategory) {
            case SONGS -> {
                int offset = searchOffset;
                if (searchLoading || !searchHasMore) {
                    return;
                }
                searchLoading = true;
                METADATA_EXECUTOR.submit(() -> loadSearchPage(query, offset, generation));
            }
            case PLAYLISTS -> {
                int offset = playlistSearchOffset;
                if (playlistSearchLoading || !playlistSearchHasMore) {
                    return;
                }
                playlistSearchLoading = true;
                METADATA_EXECUTOR.submit(() -> loadPlaylistSearchPage(query, offset, generation));
            }
            case USERS -> {
                int offset = userSearchOffset;
                if (userSearchLoading || !userSearchHasMore) {
                    return;
                }
                userSearchLoading = true;
                METADATA_EXECUTOR.submit(() -> loadUserSearchPage(query, offset, generation));
            }
        }
    }

    private static void loadSearchPage(String query, int offset, long generation) {
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = SEARCH_ENDPOINT + "?csrf_token=&s=" + encoded
                    + "&type=1&offset=" + offset + "&total=true&limit=" + SEARCH_PAGE_SIZE;
            JsonObject root = JsonParser.parseString(get(url)).getAsJsonObject();
            JsonObject resultObject = root.getAsJsonObject("result");
            JsonArray songs = resultObject == null ? null : resultObject.getAsJsonArray("songs");
            List<Song> page = new ArrayList<>();
            if (songs != null) {
                for (JsonElement element : songs) {
                    JsonObject song = element.getAsJsonObject();
                    JsonObject album = song.getAsJsonObject("album");
                    page.add(new Song(song.get("id").getAsLong(),
                            text(song, "name"), artist(song),
                            album == null ? "Unknown Album" : text(album, "name"),
                            album == null ? "" : text(album, "picUrl"),
                            number(song, "duration", number(song, "dt", 0L)),
                            (int) number(song, "fee", 0L)));
                }
            }
            try {
                page = attachCoverUrls(page);
            } catch (Exception ignored) {
                // Search results remain usable when the detail endpoint is unavailable.
            }
            synchronized (NeteaseMusicService.class) {
                if (generation != searchGeneration) {
                    return;
                }
                List<Song> combined = new ArrayList<>(offset == 0 ? List.of() : searchResults);
                combined.addAll(page);
                searchResults = List.copyOf(combined);
                searchOffset = offset + page.size();
                searchTotal = resultObject == null ? 0 : number(resultObject, "songCount", 0L) > Integer.MAX_VALUE
                        ? Integer.MAX_VALUE : (int) number(resultObject, "songCount", 0L);
                searchHasMore = !page.isEmpty()
                        && (searchTotal <= 0 || searchOffset < searchTotal)
                        && page.size() >= SEARCH_PAGE_SIZE;
                songSearchStatus = searchResults.isEmpty() ? "No songs" : searchResults.size() + " songs";
            }
        } catch (Exception exception) {
            if (generation == searchGeneration && offset == 0) {
                searchResults = List.of();
                songSearchStatus = "Song search failed";
            }
        } finally {
            if (generation == searchGeneration) {
                searchLoading = false;
            }
        }
    }

    private static void loadPlaylistSearchPage(String query, int offset, long generation) {
        try {
            JsonObject resultObject = searchResult(query, SearchCategory.PLAYLISTS, offset);
            JsonArray values = resultObject == null ? null : resultObject.getAsJsonArray("playlists");
            List<Playlist> page = new ArrayList<>();
            if (values != null) {
                for (JsonElement element : values) {
                    JsonObject playlist = element.getAsJsonObject();
                    page.add(playlistFromJson(playlist));
                }
            }
            synchronized (NeteaseMusicService.class) {
                if (generation != searchGeneration) {
                    return;
                }
                List<Playlist> combined = new ArrayList<>(offset == 0 ? List.of() : playlistSearchResults);
                combined.addAll(page);
                playlistSearchResults = List.copyOf(combined);
                playlistSearchOffset = offset + page.size();
                playlistSearchTotal = resultCount(resultObject, "playlistCount");
                playlistSearchHasMore = hasMore(page.size(), playlistSearchOffset, playlistSearchTotal);
                playlistSearchStatus = playlistSearchResults.isEmpty()
                        ? "No playlists" : playlistSearchResults.size() + " playlists";
            }
        } catch (Exception exception) {
            if (generation == searchGeneration && offset == 0) {
                playlistSearchResults = List.of();
                playlistSearchStatus = "Playlist search failed";
            }
        } finally {
            if (generation == searchGeneration) {
                playlistSearchLoading = false;
            }
        }
    }

    private static void loadUserSearchPage(String query, int offset, long generation) {
        try {
            JsonObject resultObject = searchResult(query, SearchCategory.USERS, offset);
            JsonArray values = resultObject == null ? null : resultObject.getAsJsonArray("userprofiles");
            List<UserProfile> page = new ArrayList<>();
            if (values != null) {
                for (JsonElement element : values) {
                    JsonObject user = element.getAsJsonObject();
                    page.add(new UserProfile(
                            number(user, "userId", 0L), text(user, "nickname"),
                            text(user, "avatarUrl"), text(user, "signature"),
                            (int) number(user, "playlistCount", 0L),
                            number(user, "followeds", 0L)));
                }
            }
            synchronized (NeteaseMusicService.class) {
                if (generation != searchGeneration) {
                    return;
                }
                List<UserProfile> combined = new ArrayList<>(offset == 0 ? List.of() : userSearchResults);
                combined.addAll(page);
                userSearchResults = List.copyOf(combined);
                userSearchOffset = offset + page.size();
                userSearchTotal = resultCount(resultObject, "userprofileCount");
                userSearchHasMore = hasMore(page.size(), userSearchOffset, userSearchTotal);
                userSearchStatus = userSearchResults.isEmpty()
                        ? "No users" : userSearchResults.size() + " users";
            }
        } catch (Exception exception) {
            if (generation == searchGeneration && offset == 0) {
                userSearchResults = List.of();
                userSearchStatus = "User search failed";
            }
        } finally {
            if (generation == searchGeneration) {
                userSearchLoading = false;
            }
        }
    }

    private static JsonObject searchResult(String query, SearchCategory category, int offset) throws Exception {
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = SEARCH_ENDPOINT + "?csrf_token=&s=" + encoded
                + "&type=" + category.apiType + "&offset=" + offset
                + "&total=true&limit=" + SEARCH_PAGE_SIZE;
        JsonObject root = JsonParser.parseString(get(url)).getAsJsonObject();
        return root.getAsJsonObject("result");
    }

    private static int resultCount(JsonObject result, String key) {
        long count = result == null ? 0L : number(result, key, 0L);
        return count > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) count;
    }

    private static boolean hasMore(int pageSize, int nextOffset, int total) {
        return pageSize > 0 && (total <= 0 || nextOffset < total) && pageSize >= SEARCH_PAGE_SIZE;
    }

    private static Playlist playlistFromJson(JsonObject playlist) {
        return new Playlist(
                number(playlist, "id", 0L), text(playlist, "name"),
                text(playlist, "coverImgUrl"),
                (int) number(playlist, "trackCount", 0L),
                number(playlist, "playCount", 0L));
    }

    public static void play(Song song) {
        if (song == null) {
            return;
        }
        synchronized (NeteaseMusicService.class) {
            currentSong = song;
            currentIndex = searchResults.indexOf(song);
            stopLocked();
            long generation = ++playbackGeneration;
            status = "Loading...";
            playbackTask = AUDIO_EXECUTOR.submit(() -> loadAndPlay(song, generation, 0, 0L));
        }
    }

    public static void togglePlay() {
        Song song = currentSong;
        if (song == null) {
            return;
        }
        if (playing) {
            pause();
        } else if (paused) {
            resume();
        } else {
            play(song);
        }
    }

    public static void pause() {
        synchronized (NeteaseMusicService.class) {
            if (!playing) {
                return;
            }
            TrackingPlayer active = player;
            if (active != null) {
                elapsedBeforeStartMs = elapsedMilliseconds();
                resumeFrame = seekFrameLimit(elapsedBeforeStartMs);
                active.close();
            }
            if (active == null && startedAt > 0L) {
                elapsedBeforeStartMs += Math.max(0L, System.currentTimeMillis() - startedAt);
            }
            startedAt = 0L;
            playing = false;
            paused = true;
            player = null;
            playbackGeneration++;
            Future<?> task = playbackTask;
            playbackTask = null;
            if (task != null) {
                task.cancel(true);
            }
            status = "Paused";
        }
    }

    public static void resume() {
        synchronized (NeteaseMusicService.class) {
            if (!paused || currentSong == null) {
                return;
            }
            paused = false;
            long generation = ++playbackGeneration;
            status = "Loading...";
            int startFrame = resumeFrame;
            Song song = currentSong;
            playbackTask = AUDIO_EXECUTOR.submit(() -> loadAndPlay(song, generation, startFrame, elapsedBeforeStartMs));
        }
    }

    public static void stop() {
        synchronized (NeteaseMusicService.class) {
            stopLocked();
            playbackGeneration++;
            status = currentSong == null ? "Ready" : "Stopped";
        }
    }

    public static void next() {
        if (searchResults.isEmpty()) {
            return;
        }
        Song next = adjacentPlayableSong(currentIndex, 1, true);
        if (next != null) {
            play(next);
        } else {
            status = "No playable songs";
        }
    }

    public static void previous() {
        if (searchResults.isEmpty()) {
            return;
        }
        Song previous = adjacentPlayableSong(currentIndex < 0 ? 0 : currentIndex, -1, true);
        if (previous != null) {
            play(previous);
        } else {
            status = "No playable songs";
        }
    }

    public static PlayMode playMode() {
        return playMode;
    }

    public static String playModeLabel() {
        return playMode.label;
    }

    public static void cyclePlayMode() {
        PlayMode[] modes = PlayMode.values();
        playMode = modes[(playMode.ordinal() + 1) % modes.length];
    }

    public static float volume() {
        return volume;
    }

    public static void setVolume(float value) {
        volume = Math.max(0.0f, Math.min(1.0f, value));
    }

    public static List<Song> searchResults() {
        return searchResults;
    }

    public static List<Playlist> playlistSearchResults() {
        return playlistSearchResults;
    }

    public static List<UserProfile> userSearchResults() {
        return userSearchResults;
    }

    public static SearchCategory searchCategory() {
        return searchCategory;
    }

    public static boolean hasActiveSearch() {
        return !searchQuery.isBlank();
    }

    public static String activeSearchQuery() {
        return searchQuery;
    }

    public static synchronized boolean navigateBack() {
        BrowseState previous = NAVIGATION_HISTORY.pollFirst();
        if (previous == null) {
            return false;
        }
        searchGeneration++;
        resultTitle = previous.resultTitle();
        searchQuery = previous.searchQuery();
        searchResults = previous.songs();
        playlistSearchResults = previous.playlists();
        userSearchResults = previous.users();
        searchCategory = previous.category();
        searchOffset = previous.songOffset();
        searchTotal = previous.songTotal();
        searchHasMore = previous.songHasMore();
        playlistSearchOffset = previous.playlistOffset();
        playlistSearchTotal = previous.playlistTotal();
        playlistSearchHasMore = previous.playlistHasMore();
        userSearchOffset = previous.userOffset();
        userSearchTotal = previous.userTotal();
        userSearchHasMore = previous.userHasMore();
        songSearchStatus = previous.songStatus();
        playlistSearchStatus = previous.playlistStatus();
        userSearchStatus = previous.userStatus();
        searchLoading = false;
        playlistSearchLoading = false;
        userSearchLoading = false;
        currentIndex = currentSong == null ? -1 : searchResults.indexOf(currentSong);
        status = searchStatus(searchCategory);
        resumeRestoredSearch();
        return true;
    }

    public static synchronized void clearNavigationHistory() {
        NAVIGATION_HISTORY.clear();
    }

    private static void pushNavigationState() {
        NAVIGATION_HISTORY.addFirst(new BrowseState(
                resultTitle, searchQuery, searchResults, playlistSearchResults, userSearchResults,
                searchCategory,
                searchOffset, searchTotal, searchHasMore,
                playlistSearchOffset, playlistSearchTotal, playlistSearchHasMore,
                userSearchOffset, userSearchTotal, userSearchHasMore,
                songSearchStatus, playlistSearchStatus, userSearchStatus));
    }

    private static void resumeRestoredSearch() {
        if (searchQuery.isBlank()) {
            return;
        }
        long generation = searchGeneration;
        String restoredQuery = searchQuery;
        if (searchResults.isEmpty() && searchHasMore) {
            searchLoading = true;
            METADATA_EXECUTOR.submit(() -> loadSearchPage(restoredQuery, 0, generation));
        }
        if (playlistSearchResults.isEmpty() && playlistSearchHasMore) {
            playlistSearchLoading = true;
            METADATA_EXECUTOR.submit(() -> loadPlaylistSearchPage(restoredQuery, 0, generation));
        }
        if (userSearchResults.isEmpty() && userSearchHasMore) {
            userSearchLoading = true;
            METADATA_EXECUTOR.submit(() -> loadUserSearchPage(restoredQuery, 0, generation));
        }
    }

    public static void setSearchCategory(SearchCategory category) {
        if (category != null) {
            searchCategory = category;
        }
    }

    public static String searchStatus(SearchCategory category) {
        return switch (category == null ? SearchCategory.SONGS : category) {
            case SONGS -> songSearchStatus;
            case PLAYLISTS -> playlistSearchStatus;
            case USERS -> userSearchStatus;
        };
    }

    public static Song currentSong() {
        return currentSong;
    }

    public static String status() {
        return status;
    }

    public static boolean playing() {
        return playing;
    }

    public static boolean paused() {
        return paused;
    }

    public static long lyricTimelineRevision() {
        return lyricTimelineRevision;
    }

    public static long elapsedSeconds() {
        return elapsedMilliseconds() / 1000L;
    }

    public static long elapsedMilliseconds() {
        TrackingPlayer active = player;
        if (playing && active != null) {
            if (active.hasOutputStarted()) {
                return Math.max(0L, active.timelinePositionMs());
            }
            return Math.max(0L, elapsedBeforeStartMs);
        }
        long elapsed = elapsedBeforeStartMs;
        if (playing && startedAt > 0L) {
            elapsed += Math.max(0L, System.currentTimeMillis() - startedAt);
        }
        return Math.max(0L, elapsed);
    }

    public static void seekToMillis(long positionMs) {
        synchronized (NeteaseMusicService.class) {
            if (currentSong == null || currentSong.durationMs() <= 0L) {
                return;
            }
            long target = Math.max(0L, Math.min(positionMs, currentSong.durationMs()));
            boolean resumePlayback = playing;
            boolean keepPaused = paused && !playing;
            stopLocked();
            playbackGeneration++;
            elapsedBeforeStartMs = target;
            resumeFrame = seekFrameLimit(target);
            lyricSeekPending = true;
            lyricTimelineRevision++;
            if (keepPaused || !resumePlayback) {
                paused = keepPaused;
                lyricSeekPending = false;
                lyricCursorIndex = -1;
                lyricCursorPositionMs = -1L;
                lyricTimelineRevision++;
                status = keepPaused ? "Paused" : "Ready";
                return;
            }
            long generation = ++playbackGeneration;
            Song song = currentSong;
            status = "Loading...";
            playbackTask = AUDIO_EXECUTOR.submit(() -> loadAndPlay(song, generation, resumeFrame, target));
        }
    }

    public static List<LyricLine> lyrics(Song song) {
        if (song == null) {
            return List.of();
        }
        List<LyricLine> cached = LYRICS.get(song.id());
        if (cached != null) {
            return cached;
        }
        requestLyrics(song);
        return List.of();
    }

    public static String lyricMessage(Song song) {
        if (song == null) {
            return "";
        }
        if (!LYRICS.containsKey(song.id())) {
            requestLyrics(song);
            return "";
        }
        return LYRIC_MESSAGES.getOrDefault(song.id(), "");
    }

    public static synchronized int currentLyricIndex(Song song) {
        List<LyricLine> lines = lyrics(song);
        if (lines.isEmpty()) {
            return -1;
        }
        if (song.id() != lyricCursorSongId) {
            lyricCursorSongId = song.id();
            lyricCursorIndex = -1;
            lyricCursorPositionMs = -1L;
        }
        if (lyricSeekPending) {
            TrackingPlayer active = player;
            if ((playing && (active == null || !active.hasOutputStarted()))
                    || (!playing && "Loading...".equals(status))) {
                return lyricCursorIndex;
            }
            lyricSeekPending = false;
            lyricCursorIndex = -1;
            lyricCursorPositionMs = -1L;
            lyricTimelineRevision++;
        }
        long position = Math.max(0L, elapsedMilliseconds() - LYRIC_SYNC_DELAY_MS);
        if (lyricCursorPositionMs >= 0L && position + 350L < lyricCursorPositionMs) {
            lyricCursorIndex = -1;
            lyricCursorPositionMs = position;
        }
        if (position >= lyricCursorPositionMs) {
            while (lyricCursorIndex + 1 < lines.size()
                    && lines.get(lyricCursorIndex + 1).timeMs() <= position) {
                lyricCursorIndex++;
            }
            lyricCursorPositionMs = position;
        }
        return lyricCursorIndex;
    }

    public static int coverTextureId(Song song) {
        if (song == null || song.coverUrl().isBlank()) {
            return -1;
        }
        return coverTextureId(song.id(), song.coverUrl());
    }

    public static int playlistCoverTextureId(Playlist playlist) {
        if (playlist == null || playlist.coverUrl().isBlank()) {
            return -1;
        }
        return coverTextureId(playlist.id() ^ Long.MIN_VALUE, playlist.coverUrl());
    }

    public static int userAvatarTextureId(UserProfile user) {
        if (user == null || user.avatarUrl().isBlank()) {
            return -1;
        }
        return coverTextureId(user.id() ^ 0x5A5A5A5A5A5A5A5AL, user.avatarUrl());
    }

    private static int coverTextureId(long textureKey, String coverUrl) {
        ResourceLocation texture = COVER_TEXTURES.get(textureKey);
        if (texture == null) {
            requestCover(textureKey, coverUrl);
            return -1;
        }
        return Minecraft.getInstance().getTextureManager().getTexture(texture).getId();
    }

    private static void loadAndPlay(Song song, long generation, int startFrame, long startPositionMs) {
        try {
            String audioUrl = currentAudioUrl;
            boolean preview = currentAudioPreview;
            if (audioUrl == null || audioUrl.isBlank()) {
                String ids = "[" + song.id() + "]";
                String url = URL_ENDPOINT + "?ids=" + URLEncoder.encode(ids, StandardCharsets.UTF_8)
                        + "&level=standard&encodeType=mp3";
                JsonObject root = JsonParser.parseString(get(url)).getAsJsonObject();
                JsonArray data = root.getAsJsonArray("data");
                if (data != null && !data.isEmpty()) {
                    JsonObject playbackData = data.get(0).getAsJsonObject();
                    JsonElement urlValue = playbackData.get("url");
                    if (urlValue != null && !urlValue.isJsonNull()) {
                        audioUrl = urlValue.getAsString();
                    }
                    JsonElement trialValue = playbackData.get("freeTrialInfo");
                    preview = trialValue != null && !trialValue.isJsonNull();
                }
            }
            if (generation != playbackGeneration) {
                return;
            }
            if (audioUrl == null || audioUrl.isBlank()) {
                status = song.vipRequired() ? "VIP required" : "No playable URL";
                return;
            }
            currentAudioUrl = audioUrl;
            currentAudioPreview = preview;
            HttpRequest audioRequest = HttpRequest.newBuilder(URI.create(audioUrl))
                    .header("User-Agent", "Xerion/1.0.5")
                    .header("Referer", "https://music.163.com/")
                    .GET()
                    .build();
            HttpResponse<InputStream> audioResponse = HTTP.send(audioRequest,
                    HttpResponse.BodyHandlers.ofInputStream());
            String contentType = audioResponse.headers().firstValue("Content-Type").orElse("");
            if (audioResponse.statusCode() < 200 || audioResponse.statusCode() >= 300
                    || !contentType.toLowerCase(java.util.Locale.ROOT).startsWith("audio/")) {
                audioResponse.body().close();
                status = "No playable URL";
                return;
            }
            try (InputStream input = audioResponse.body()) {
                TrackingPlayer nextPlayer = new TrackingPlayer(input, startFrame, startPositionMs);
                synchronized (NeteaseMusicService.class) {
                    if (generation != playbackGeneration) {
                        nextPlayer.close();
                        return;
                    }
                    player = nextPlayer;
                    playing = true;
                    paused = false;
                    startedAt = System.currentTimeMillis();
                    status = preview ? "Playing preview" : "Playing";
                }
                nextPlayer.play(startFrame, Integer.MAX_VALUE);
                Song nextSong = null;
                synchronized (NeteaseMusicService.class) {
                    if (generation == playbackGeneration) {
                        playing = false;
                        player = null;
                        if (!paused) {
                            resumeFrame = 0;
                            elapsedBeforeStartMs = 0L;
                            startedAt = 0L;
                            nextSong = nextSongAfterCompletion(song);
                            status = nextSong == null ? "Finished" : "Loading...";
                        }
                    }
                }
                if (nextSong != null) {
                    play(nextSong);
                }
            }
        } catch (Exception exception) {
            synchronized (NeteaseMusicService.class) {
                if (generation == playbackGeneration) {
                    playing = false;
                    player = null;
                    status = "Playback failed";
                }
            }
        }
    }

    private static void stopLocked() {
        playing = false;
        paused = false;
        startedAt = 0L;
        elapsedBeforeStartMs = 0L;
        resumeFrame = 0;
        currentAudioUrl = null;
        currentAudioPreview = false;
        TrackingPlayer active = player;
        player = null;
        if (active != null) {
            active.close();
        }
        Future<?> task = playbackTask;
        playbackTask = null;
        if (task != null) {
            task.cancel(true);
        }
    }

    private static Song nextSongAfterCompletion(Song completedSong) {
        if (playMode == PlayMode.SINGLE) {
            return completedSong;
        }
        if (searchResults.isEmpty()) {
            return null;
        }
        int index = searchResults.indexOf(completedSong);
        if (index < 0) {
            return null;
        }
        if (playMode == PlayMode.SHUFFLE && searchResults.size() > 1) {
            List<Song> candidates = new ArrayList<>();
            for (Song song : searchResults) {
                if (song.id() != completedSong.id() && !song.vipRequired()) {
                    candidates.add(song);
                }
            }
            return candidates.isEmpty() ? null : candidates.get(
                    java.util.concurrent.ThreadLocalRandom.current().nextInt(candidates.size()));
        }
        return adjacentPlayableSong(index, 1, false);
    }

    private static Song adjacentPlayableSong(int startIndex, int direction, boolean wrap) {
        int size = searchResults.size();
        if (size == 0) {
            return null;
        }
        for (int step = 1; step <= size; step++) {
            int index = startIndex + direction * step;
            if (wrap) {
                index = Math.floorMod(index, size);
            } else if (index < 0 || index >= size) {
                return null;
            }
            Song candidate = searchResults.get(index);
            if (!candidate.vipRequired()) {
                return candidate;
            }
        }
        return null;
    }

    private static String get(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("User-Agent", "Xerion/1.0.5")
                .header("Referer", "https://music.163.com/")
                .GET()
                .build();
        HttpResponse<String> response = HTTP.send(request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("HTTP " + response.statusCode());
        }
        return response.body();
    }

    private static String artist(JsonObject song) {
        JsonArray artists = song.getAsJsonArray("artists");
        if (artists == null || artists.isEmpty()) {
            return "Unknown Artist";
        }
        return text(artists.get(0).getAsJsonObject(), "name");
    }

    private static String text(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value == null || value.isJsonNull() ? "" : value.getAsString();
    }

    private static long number(JsonObject object, String key, long fallback) {
        JsonElement value = object.get(key);
        if (value == null || value.isJsonNull()) {
            return fallback;
        }
        try {
            return value.getAsLong();
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    private static List<Song> loadSongsByIds(List<Long> orderedIds) throws Exception {
        Map<Long, Song> songsById = new HashMap<>();
        int batchSize = 100;
        for (int offset = 0; offset < orderedIds.size(); offset += batchSize) {
            int end = Math.min(orderedIds.size(), offset + batchSize);
            StringBuilder ids = new StringBuilder("[");
            for (int i = offset; i < end; i++) {
                if (i > offset) {
                    ids.append(',');
                }
                ids.append(orderedIds.get(i));
            }
            ids.append(']');
            String detailUrl = "https://music.163.com/api/song/detail/?id=" + orderedIds.get(offset)
                    + "&ids=" + URLEncoder.encode(ids.toString(), StandardCharsets.UTF_8);
            JsonObject root = JsonParser.parseString(get(detailUrl)).getAsJsonObject();
            JsonArray details = root.getAsJsonArray("songs");
            if (details == null) {
                continue;
            }
            for (JsonElement element : details) {
                Song song = songFromJson(element.getAsJsonObject());
                songsById.put(song.id(), song);
            }
        }
        List<Song> songs = new ArrayList<>(orderedIds.size());
        for (long id : orderedIds) {
            Song song = songsById.get(id);
            if (song != null) {
                songs.add(song);
            }
        }
        return songs;
    }

    private static Song songFromJson(JsonObject song) {
        JsonObject album = song.getAsJsonObject("album");
        return new Song(number(song, "id", 0L), text(song, "name"), artist(song),
                album == null ? "Unknown Album" : text(album, "name"),
                album == null ? "" : text(album, "picUrl"),
                number(song, "duration", number(song, "dt", 0L)),
                (int) number(song, "fee", 0L));
    }

    private static List<Song> attachCoverUrls(List<Song> songs) throws Exception {
        if (songs.isEmpty()) {
            return songs;
        }
        StringBuilder ids = new StringBuilder("[");
        for (int i = 0; i < songs.size(); i++) {
            if (i > 0) {
                ids.append(',');
            }
            ids.append(songs.get(i).id());
        }
        ids.append(']');
        String detailUrl = "https://music.163.com/api/song/detail/?id=" + songs.get(0).id()
                + "&ids=" + URLEncoder.encode(ids.toString(), StandardCharsets.UTF_8);
        JsonObject root = JsonParser.parseString(get(detailUrl)).getAsJsonObject();
        JsonArray details = root.getAsJsonArray("songs");
        Map<Long, String> covers = new HashMap<>();
        Map<Long, Long> durations = new HashMap<>();
        if (details != null) {
            for (JsonElement element : details) {
                JsonObject detail = element.getAsJsonObject();
                long id = detail.get("id").getAsLong();
                JsonObject album = detail.getAsJsonObject("album");
                if (album != null) {
                    covers.put(id, text(album, "picUrl"));
                }
                durations.put(id, number(detail, "duration", number(detail, "dt", 0L)));
            }
        }
        List<Song> enriched = new ArrayList<>(songs.size());
        for (Song song : songs) {
            String cover = covers.getOrDefault(song.id(), song.coverUrl());
            long durationMs = durations.getOrDefault(song.id(), song.durationMs());
            enriched.add(new Song(song.id(), song.name(), song.artist(), song.album(), cover,
                    durationMs, song.fee()));
        }
        return enriched;
    }

    private static void requestCover(long textureKey, String sourceUrl) {
        if (!COVER_LOADING.add(textureKey)) {
            return;
        }
        COVER_EXECUTOR.submit(() -> {
            try {
                String coverUrl = sourceUrl + (sourceUrl.contains("?") ? "&" : "?") + "param=320y320";
                HttpRequest request = HttpRequest.newBuilder(URI.create(coverUrl))
                        .header("User-Agent", "Xerion/1.0.5")
                        .GET()
                        .build();
                HttpResponse<InputStream> response = HTTP.send(request, HttpResponse.BodyHandlers.ofInputStream());
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    response.body().close();
                    return;
                }
                NativeImage image;
                try (InputStream input = response.body()) {
                    image = NativeImage.read(input);
                }
                Minecraft.getInstance().execute(() -> {
                    try {
                        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                                "xerion", "netease_cover_" + Long.toUnsignedString(textureKey, 36));
                        Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(image));
                        COVER_TEXTURES.put(textureKey, id);
                    } finally {
                        COVER_LOADING.remove(textureKey);
                    }
                });
            } catch (Exception ignored) {
                COVER_LOADING.remove(textureKey);
            }
        });
    }

    private static void requestLyrics(Song song) {
        if (!LYRIC_LOADING.add(song.id())) {
            return;
        }
        METADATA_EXECUTOR.submit(() -> {
            try {
                String response = get(LYRIC_ENDPOINT + song.id() + "&lv=1&kv=1&tv=-1");
                JsonObject root = JsonParser.parseString(response).getAsJsonObject();
                JsonObject lyricObject = root.getAsJsonObject("lrc");
                String lyricText = lyricObject == null ? "" : text(lyricObject, "lyric");
                List<LyricLine> lines = parseLyrics(lyricText);
                LYRICS.put(song.id(), lines);
                if (lines.isEmpty()) {
                    String message = lyricMessage(root, lyricText);
                    if (message.isBlank()) {
                        LYRIC_MESSAGES.remove(song.id());
                    } else {
                        LYRIC_MESSAGES.put(song.id(), message);
                    }
                } else {
                    LYRIC_MESSAGES.remove(song.id());
                }
            } catch (Exception ignored) {
                LYRICS.put(song.id(), List.of());
                LYRIC_MESSAGES.remove(song.id());
            } finally {
                LYRIC_LOADING.remove(song.id());
            }
        });
    }

    private static String lyricMessage(JsonObject root, String lyricText) {
        String serverMessage = text(root, "message");
        if (serverMessage.isBlank()) {
            serverMessage = text(root, "msg");
        }
        if (!serverMessage.isBlank()) {
            return serverMessage;
        }
        if (booleanValue(root, "pureMusic") || booleanValue(root, "nolyric")) {
            return "纯音乐，请欣赏";
        }
        if (booleanValue(root, "uncollected")) {
            return "暂无歌词";
        }
        return lyricText == null ? "" : lyricText.replace('\r', ' ')
                .replace('\n', ' ').trim();
    }

    private static boolean booleanValue(JsonObject object, String name) {
        JsonElement value = object == null ? null : object.get(name);
        return value != null && !value.isJsonNull() && value.getAsBoolean();
    }

    private static List<LyricLine> parseLyrics(String lyricText) {
        if (lyricText == null || lyricText.isBlank()) {
            return List.of();
        }
        List<LyricLine> lines = new ArrayList<>();
        for (String rawLine : lyricText.replace("\r", "").split("\n")) {
            Matcher matcher = LYRIC_TIMESTAMP.matcher(rawLine);
            while (matcher.find()) {
                long minutes = Long.parseLong(matcher.group(1));
                long seconds = Long.parseLong(matcher.group(2));
                String fraction = matcher.group(3);
                long fractionMs = fraction == null ? 0L
                        : fraction.length() == 1 ? Long.parseLong(fraction) * 100L
                        : fraction.length() == 2 ? Long.parseLong(fraction) * 10L
                        : Long.parseLong(fraction.substring(0, 3));
                String text = matcher.group(4).trim();
                if (!text.isEmpty()) {
                    lines.add(new LyricLine(minutes * 60_000L + seconds * 1_000L + fractionMs, text));
                }
            }
        }
        lines.sort(Comparator.comparingLong(LyricLine::timeMs));
        return lines.isEmpty() ? List.of() : List.copyOf(lines);
    }

    public record Song(long id, String name, String artist, String album, String coverUrl,
                       long durationMs, int fee) {
        public boolean vipRequired() {
            return fee == 1 || fee == 4;
        }
    }

    public record Playlist(long id, String name, String coverUrl, int trackCount, long playCount) {
    }

    public record UserProfile(long id, String nickname, String avatarUrl, String signature,
                              int playlistCount, long followers) {
    }

    public enum SearchCategory {
        SONGS("Songs", 1),
        PLAYLISTS("Playlists", 1000),
        USERS("Users", 1002);

        private final String label;
        private final int apiType;

        SearchCategory(String label, int apiType) {
            this.label = label;
            this.apiType = apiType;
        }

        public String label() {
            return label;
        }
    }

    private record BrowseState(String resultTitle, String searchQuery,
                               List<Song> songs, List<Playlist> playlists, List<UserProfile> users,
                               SearchCategory category,
                               int songOffset, int songTotal, boolean songHasMore,
                               int playlistOffset, int playlistTotal, boolean playlistHasMore,
                               int userOffset, int userTotal, boolean userHasMore,
                               String songStatus, String playlistStatus, String userStatus) {
    }

    public record LyricLine(long timeMs, String text) {
    }

    private static final class TrackingPlayer extends AdvancedPlayer {
        private final int startFrame;
        private final long startPositionMs;
        private final AudioDevice audioDevice;
        private double skippedPositionMs;
        private volatile long timelineStartedAtNanos;
        private volatile int decodedFrames;
        private volatile boolean outputStarted;
        private float outputVolume;
        private long volumeUpdatedAtNanos;

        private TrackingPlayer(InputStream input, int startFrame, long startPositionMs)
                throws JavaLayerException {
            this(input, startFrame, startPositionMs,
                    FactoryRegistry.systemRegistry().createAudioDevice());
        }

        private TrackingPlayer(InputStream input, int startFrame, long startPositionMs,
                               AudioDevice audioDevice) throws JavaLayerException {
            super(input, audioDevice);
            this.startFrame = Math.max(0, startFrame);
            this.startPositionMs = Math.max(0L, startPositionMs);
            this.audioDevice = audioDevice;
            this.outputVolume = volume;
            this.volumeUpdatedAtNanos = System.nanoTime();
            applyVolume(outputVolume);
        }

        @Override
        protected boolean skipFrame() throws JavaLayerException {
            if (startPositionMs <= 0L || skippedPositionMs >= startPositionMs) {
                return false;
            }
            if (ADVANCED_PLAYER_BITSTREAM_FIELD == null) {
                return super.skipFrame();
            }
            try {
                Bitstream bitstream = (Bitstream) ADVANCED_PLAYER_BITSTREAM_FIELD.get(this);
                Header header = bitstream.readFrame();
                if (header == null) {
                    return false;
                }
                float frameDurationMs = header.ms_per_frame();
                skippedPositionMs += frameDurationMs > 0.0f ? frameDurationMs : 26.0f;
                bitstream.closeFrame();
                return skippedPositionMs < startPositionMs;
            } catch (BitstreamException | ReflectiveOperationException | RuntimeException exception) {
                throw new JavaLayerException("Unable to seek MP3 stream", exception);
            }
        }

        @Override
        protected boolean decodeFrame() throws JavaLayerException {
            if (timelineStartedAtNanos == 0L) {
                timelineStartedAtNanos = System.nanoTime();
            }
            updateOutputVolume();
            boolean decoded = super.decodeFrame();
            if (decoded) {
                applyVolume(outputVolume);
                decodedFrames++;
                outputStarted = true;
            }
            return decoded;
        }

        private int startFrame() {
            return startFrame;
        }

        private int decodedFrames() {
            return decodedFrames;
        }

        private boolean hasOutputStarted() {
            return outputStarted;
        }

        private long timelinePositionMs() {
            long startNanos = timelineStartedAtNanos;
            long elapsedMs = startNanos <= 0L
                    ? 0L : Math.max(0L, System.nanoTime() - startNanos) / 1_000_000L;
            return playbackStartPositionMs() + elapsedMs;
        }

        private long playbackStartPositionMs() {
            if (startPositionMs <= 0L || ADVANCED_PLAYER_BITSTREAM_FIELD == null) {
                return startPositionMs;
            }
            return Math.max(0L, Math.round(skippedPositionMs));
        }

        private void updateOutputVolume() {
            long now = System.nanoTime();
            double deltaSeconds = Math.min(MAX_VOLUME_LERP_DELTA_SECONDS,
                    Math.max(0.0D, (now - volumeUpdatedAtNanos) / 1_000_000_000.0D));
            volumeUpdatedAtNanos = now;
            float targetVolume = volume;
            float framesAt60Fps = (float) (deltaSeconds * 60.0D);
            float factor = 1.0f - (float) Math.pow(
                    1.0f - VOLUME_LERP_PER_FRAME_AT_60_FPS, framesAt60Fps);
            outputVolume += (targetVolume - outputVolume) * factor;
            if (Math.abs(outputVolume - targetVolume) < 0.0005f) {
                outputVolume = targetVolume;
            }
            applyVolume(outputVolume);
        }

        private void applyVolume(float value) {
            try {
                Object source = JAVA_SOUND_SOURCE_FIELD == null ? null : JAVA_SOUND_SOURCE_FIELD.get(audioDevice);
                if (!(source instanceof SourceDataLine line)) {
                    return;
                }
                FloatControl control;
                if (line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    control = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
                    float gain = value <= 0.001f ? control.getMinimum()
                            : (float) (20.0 * Math.log10(value));
                    control.setValue(Math.max(control.getMinimum(), Math.min(control.getMaximum(), gain)));
                } else if (line.isControlSupported(FloatControl.Type.VOLUME)) {
                    control = (FloatControl) line.getControl(FloatControl.Type.VOLUME);
                    control.setValue(Math.max(control.getMinimum(),
                            Math.min(control.getMaximum(), value)));
                }
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                // Some output devices expose no software volume control.
            }
        }
    }

    private static Field findJavaSoundSourceField() {
        try {
            Field field = javazoom.jl.player.JavaSoundAudioDevice.class.getDeclaredField("source");
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    private static Field findAdvancedPlayerBitstreamField() {
        try {
            Field field = AdvancedPlayer.class.getDeclaredField("bitstream");
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    private static int seekFrameLimit(long positionMs) {
        if (positionMs <= 0L) {
            return 0;
        }
        if (ADVANCED_PLAYER_BITSTREAM_FIELD == null) {
            return Math.max(1, (int) Math.round(positionMs / 26.0));
        }
        return (int) Math.min(Integer.MAX_VALUE - 1L, positionMs / 8L + 2L);
    }

    public enum PlayMode {
        SINGLE("单曲循环"),
        SHUFFLE("随机播放"),
        ORDER("顺序播放");

        private final String label;

        PlayMode(String label) {
            this.label = label;
        }
    }
}
