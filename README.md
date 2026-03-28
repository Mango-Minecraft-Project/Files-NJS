# FilesJS - NekoJS File Management Addon Module

FilesJS is a powerful addon for NekoJS 6 designed to handle your file operations with ease. Just kidding—it's not out to
steal your Discord account or wallet!

## Features

- **File Operations**: Read, write, append, copy, move, and delete files.
- **Directory Management**: List, create, and monitor directories.
- **File Monitoring**: Track file changes and content modifications.
- **File Analysis**: Compare files, calculate MD5 hashes, and search file contents.
- **Backup Management**: Create and manage file backups.
- **Archiving Operations**: Generate ZIP archives.

## Partial API Reference

### File Reading and Writing

```javascript
// Read file contents
let content = FilesJS.readFile('NekoJS/config/myconfig.txt');

// Write to a file
FilesJS.writeFile('NekoJS/data/output.txt', 'Hello, World!');

// Append to a file
FilesJS.appendFile('NekoJS/logs/mylog.txt', 'New log entry');

// Append a single line
FilesJS.appendLine('NekoJS/logs/mylog.txt', 'A new line');

// Write multiple lines
FilesJS.writeLines('NekoJS/data/lines.txt', ['Line 1', 'Line 2', 'Line 3']);

// Read all lines
let lines = FilesJS.readLines('NekoJS/data/lines.txt');

// Read the last N lines
let lastLines = FilesJS.readLastLines('NekoJS/logs/latest.log', 10);

// Save a script file (auto-adds `.js` extension and timestamp comments)
FilesJS.saveScript('NekoJS/scripts/newscript', 'console.log("Hello");');
```

### File Management Operations

```javascript
// Check if a file exists
if (FilesJS.exists('NekoJS/scripts/myscript.js')) {
    // Perform an operation
}

// Delete a file
FilesJS.delete('NekoJS/temp/oldfile.txt');

// Copy a file
FilesJS.copy('source.txt', 'target.txt');

// Move a file
FilesJS.move('old/path.txt', 'new/path.txt');

// Rename a file
FilesJS.renameFile('oldname.txt', 'newname.txt');

// Create a directory
FilesJS.createDirectory('NekoJS/newdir');

// Check if a file is empty
let isEmpty = FilesJS.isFileEmpty('NekoJS/data/file.txt');

// Get MD5 hash of a file
let hash = FilesJS.getFileMD5('NekoJS/data/important.dat');

// Compare two files
let areEqual = FilesJS.compareFiles('file1.txt', 'file2.txt');
```

### Bulk File Operations

```javascript
// Merge multiple files
FilesJS.mergeFiles(['file1.txt', 'file2.txt'], 'merged.txt');

// Batch copy files (using wildcards)
FilesJS.copyFiles('source/dir', 'target/dir', '*.json');

// Create a ZIP archive
FilesJS.createZip('NekoJS/data', 'NekoJS/backups/data.zip');

// Replace content in a file
FilesJS.replaceInFile('config.txt', 'old value', 'new value');
```

### Directory Operations

```javascript
// List files in a directory
let files = FilesJS.listFiles('NekoJS/data');

// Recursively list all files
let allFiles = FilesJS.listFilesRecursively('NekoJS/scripts');

// Get file information
let fileInfo = FilesJS.getFileInfo('NekoJS/config/settings.json');
// fileInfo includes: exists, size, lastModified, isDirectory, isFile, isReadable, isWritable
```

### File Monitoring

```javascript
// Monitor directory changes
FilesJS.watchDirectory('NekoJS/data', (changedPath) => {
    console.log('File changed:', changedPath);
});

// Monitor file content changes (with similarity threshold)
FilesJS.watchContentChanges('NekoJS/config/dynamic.json', 0.1);

// Monitor files matching a specific pattern
FilesJS.watchFilePattern('NekoJS/scripts', '*.js');

// Monitor file size
FilesJS.watchFileSize('NekoJS/data/growing.log', 1024 * 1024); // 1MB threshold
```

### Backup System

```javascript
// Create an immediate backup
FilesJS.backupFile('NekoJS/important/data.json');

// Schedule a backup (delayed ticks)
FilesJS.scheduleBackup('NekoJS/config/settings.json', 100); // Backup after 100 ticks

// Search file contents
let matches = FilesJS.searchInFile('NekoJS/logs/latest.log', 'ERROR');
```

## Security and Limitations

### Allowed Directories

Accessible files must be within these directories:

- `NekoJS/`
- `config/`
- `logs/`
- `backups/`
- `scripts/`

### File Size Limits

- Maximum file size: 5MB
- Content size for write operations: 5MB

### File Type Restrictions

Allowed extensions include:

- `.txt`, `.json`, `.js`, `.log`, `.cfg`, `.toml`, `.properties`, `.backup`

### Safety Measures

- Restricted access to files outside the Minecraft instance directory.
- No parent directory traversal (`..`).
- Critical operations automatically create backups.
- Validation checks for file access, size, type, and path.

## Error Handling

Use `try` and `catch` for exception handling:

```javascript
try {
    Files.writeFile('NekoJS/data/test.txt', 'Content');
} catch (e) {
    console.error('Failed to write file:', e.message);
}
```

## Best Practices

1. Always handle exceptions in scripts.
2. Use proper file extensions.
3. Check file size before operations.
4. Clean up temporary files promptly.
5. Leverage backups for critical files.
6. Monitor file changes carefully.
7. Validate file content before writing.
8. Log events using the event system.
9. Implement error recovery mechanisms.
10. Regularly clean up old backups.

## Performance Considerations

1. Be mindful of memory usage with large files.
2. Avoid frequent file monitoring.
3. Use bulk operations over single operations.
4. Set appropriate monitoring thresholds.

## License

This project is licensed under the MIT License. See the LICENSE file for details.

## Troubleshooting

1. Ensure path/file restrictions are adhered to.
2. Operate only within the allowed directories.
3. Check file size and type constraints.
4. Review logs for detailed error information.

---

#### Credits & Licensing

This project is a port of [FilesJS](https://www.curseforge.com/minecraft/mc-mods/kubejs-filesjs)
by [LirxOwO](https://github.com/xiaoliziawa/) to the NekoJS platform.

* **Original Project:** [xiaoliziawa/Files-JS](https://github.com/xiaoliziawa/Files-JS)
* **License:** The original code is licensed under
  the [MIT License](https://github.com/xiaoliziawa/Files-JS/blob/main/LICENSE).