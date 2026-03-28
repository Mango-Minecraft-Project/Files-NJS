package dev.mangojellypudding.files_njs.nekojs;

import com.tkisor.nekojs.api.event.EventBusJS;
import com.tkisor.nekojs.api.event.EventGroup;

public interface FileEvents {
    EventGroup GROUP = EventGroup.of("FileEvents");

    EventBusJS<FileEventJS, Void>
            FILE_CREATED = GROUP.server("fileCreated", FileEventJS.class),
            FILE_CHANGED = GROUP.server("fileChanged", FileEventJS.class),
            FILE_DELETED = GROUP.server("fileDeleted", FileEventJS.class),
            FILE_COPIED = GROUP.server("fileCopied", FileEventJS.class),
            FILE_MOVED = GROUP.server("fileMoved", FileEventJS.class),
            FILE_RENAMED = GROUP.server("fileRenamed", FileEventJS.class),
            FILE_BACKUP_CREATED = GROUP.server("fileBackCreated", FileEventJS.class),
            FILE_WATCH_STOPPED = GROUP.server("fileWatchStopped", FileEventJS.class),
            FILE_ACCESS_DENIED = GROUP.server("fileAccessDenied", FileEventJS.class),
            FILE_SIZE_THRESHOLD = GROUP.server("fileSizeThresholded", FileEventJS.class),
            FILE_PATTERN_MATCHED = GROUP.server("filePatternMatched", FileEventJS.class),
            FILE_CONTENT_CHANGED_SIGNIFICANTLY = GROUP.server("fileContentChangedSignificantly", FileEventJS.class),
            FILES_MERGED = GROUP.server("filesMerged", FileEventJS.class),
            DIRECTORY_CREATED = GROUP.server("directoryCreated", FileEventJS.class),
            DIRECTORY_DELETED = GROUP.server("directoryDeleted", FileEventJS.class);
}
