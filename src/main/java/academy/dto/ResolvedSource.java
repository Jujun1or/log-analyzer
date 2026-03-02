package academy.dto;

import java.net.URI;
import java.nio.file.Path;

public record ResolvedSource(boolean remote, URI uri, Path path, String displayName) {
    public ResolvedSource {
        if (remote) {
            if (uri == null) throw new IllegalArgumentException("Remote source must have non-null URI");
            if (path != null) throw new IllegalArgumentException("Remote source must have null path");
        } else {
            if (path == null) throw new IllegalArgumentException("Local source must have non-null path");
            if (uri != null) throw new IllegalArgumentException("Local source must have null uri");
        }

        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("displayName must not be empty");
        }
    }

    public static ResolvedSource createLocal(Path path) {
        return new ResolvedSource(false, null, path, path.toString());
    }

    public static ResolvedSource createRemote(URI uri) {
        return new ResolvedSource(true, uri, null, uri.toString());
    }
}
