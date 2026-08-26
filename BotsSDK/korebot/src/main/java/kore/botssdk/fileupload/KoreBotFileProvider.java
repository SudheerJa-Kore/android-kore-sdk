package kore.botssdk.fileupload;

import androidx.core.content.FileProvider;

/**
 * SDK-specific provider that avoids colliding with a host application's
 * FileProvider declaration.
 */
public final class KoreBotFileProvider extends FileProvider {
}
