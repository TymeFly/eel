package com.github.tymefly.eel;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.annotation.VisibleForTesting;

/**
 * Build {@link File} objects that do not reference sensitive parts of the local file system
 */
class SecureFileFactory implements FileFactory {
    /**
     * Platform specific rules for validating files; Windows and *nix (Unix/Linux)
     */
    @VisibleForTesting
    enum Rules {
        NIX("/",
            "/bin/",
            "/dev/",
            "/etc/",
            "/usr/bin/",
            "/usr/sbin/",
            "/usr/lib/",
            "/lib/",
            "/sbin/",
            "/var/log/",
            "/var/lock/",
            "/boot/",
            "/proc/",
            "/root/"),

        WINDOWS("\\",
                System.getenv("ProgramData"),
                System.getenv("ProgramFiles"),
                System.getenv("ProgramFiles(x86)"),
                System.getenv("ProgramW6432"),
                System.getenv("SystemRoot"),
                System.getenv("windir")) {
            @Nonnull
            @Override
            // As Windows paths are case-insensitive, convert everything to a consistent case
            String cleanPath(@Nonnull String file) {
                return super.cleanPath(file.toLowerCase());
            }
        };

        private final String separator;
        private final Collection<String> blackList;
        private final Collection<String> whiteList;


        Rules(@Nonnull String separator, String... black) {
            this.separator = separator;
            this.blackList = buildList(black);
            this.whiteList = buildList(
                System.getProperty("user.home"),
                System.getProperty("java.io.tmpdir")
            );
        }


        @Nonnull
        static Rules systemRules() {
            return ('\\' == File.separatorChar ? WINDOWS : NIX);
        }

        @Nonnull
        public Collection<String> whiteList() {
            return whiteList;
        }

        @Nonnull
        public Collection<String> blackList() {
            return blackList;
        }

        @Nonnull
        String cleanPath(@Nonnull String file) {
            if (!file.endsWith(separator)) {
                file += separator;
            }

            return file;
        }

        @Nonnull
        private Collection<String> buildList(String... entries) {
            return Arrays.stream(entries)
                .filter(Objects::nonNull)                   // nulls may occur if an env-var or property is not set
                .map(this::cleanPath)
                .collect(Collectors.toSet());               // return a set to remove duplicate entries
        }
    }

    private static final SecureFileFactory STANDARD = new SecureFileFactory(File::new);
    private static final Rules SYSTEM_RULES = Rules.systemRules();

    private final FileFactory fileFactory;


    private SecureFileFactory(@Nonnull FileFactory fileFactory) {
        this.fileFactory = fileFactory;
    }


    @Nonnull
    static SecureFileFactory standard() {
        return STANDARD;
    }

    @Nonnull
    static SecureFileFactory custom(@Nonnull FileFactory fileFactory) {
        return new SecureFileFactory(fileFactory);
    }


    @Nonnull
    @Override
    public File build(@Nonnull String path) throws IOException {
        if (path.isEmpty()) {
            throw new IOException("Empty path");
        }

        File file = fileFactory.build(path);
        File secured = secure(file);

        return secured;
    }

    @Nonnull
    private File secure(@Nonnull File file) throws IOException {
        String path = file.getPath();
        String clean;

        try {
            file = file.getCanonicalFile();
            clean = SYSTEM_RULES.cleanPath(file.getAbsolutePath());
        } catch (IOException e) {
            throw new IOException("Can not read path '" + path + "'", e);
        }

        if (!onWhiteList(SYSTEM_RULES, clean) && onBlackList(SYSTEM_RULES, clean)) {
            throw new IOException("Path '" + path + "' is in a sensitive part of the local file system");
        }

        return file;
    }


    @VisibleForTesting
    boolean onWhiteList(@Nonnull Rules rules, @Nonnull String path) {
        Collection<String> white = rules.whiteList();

        return onList(white, path);
    }

    @VisibleForTesting
    boolean onBlackList(@Nonnull Rules rules, @Nonnull String path) {
        Collection<String> black = rules.blackList();

        return onList(black, path);
    }


    private boolean onList(@Nonnull Collection<String> list, @Nonnull String path) {
        boolean found = false;

        for (var test : list) {
            found = path.startsWith(test);

            if (found) {
                break;
            }
        }

        return found;
    }
}
