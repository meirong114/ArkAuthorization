package prts.user.authorization0;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.DocumentsProvider;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import android.os.CancellationSignal;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

public class DocumentsProvider extends DocumentsProvider {
    
    private static final String TAG = "DocumentsProvider";
    private static final String AUTHORITY = "prts.user.authorization0.provider";

    // 根目录ID
    private static final String ROOT_ID = "root";

    // 各个目录的ID
    private static final String ANDROID_DATA_ID = "android_data";
    private static final String ANDROID_OBB_ID = "android_obb";
    private static final String DATA_ID = "data";
    private static final String USER_DE_DATA_ID = "user_de_data";
    private static final String OPERATORS_FILE_ID = "operators_file";
    private static final String ROOTFS_ID = "rootfs";
    private static final String DEBUGAPK_ID = "debug.apk";
    private static final String BUILDPROP_ID = "build.prop";

    // MIME类型
    private static final String MIME_TYPE_DIR = DocumentsContract.Document.MIME_TYPE_DIR;
    private static final String MIME_TYPE_APK = "application/vnd.android.package-archive";
    private static final String MIME_TYPE_TEXT = "plain/text";
    private static final String MIME_TYPE_NOTICE = getString(R.string.provider_notice);

    // 显示名称
    private static final String ROOT_DISPLAY_NAME = "";

    private static String getString(int provider_notice) {
        return String.valueOf(R.string.provider_notice);
      //return R.string.provider_notice;
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor queryRoots(String[] projection) throws FileNotFoundException {
        MatrixCursor result = new MatrixCursor(resolveRootProjection(projection));
        MatrixCursor.RowBuilder row = result.newRow();

        row.add(DocumentsContract.Root.COLUMN_ROOT_ID, ROOT_ID);
        row.add(DocumentsContract.Root.COLUMN_DOCUMENT_ID, ROOT_ID);

        // 使用资源字符串代替硬编码
        Context context = getContext();
        String title = context.getString(R.string.dc_name); // 应用数据与文件
        String summary = context.getString(R.string.dc_subname); // 描述

        row.add(DocumentsContract.Root.COLUMN_TITLE, title);
        row.add(DocumentsContract.Root.COLUMN_SUMMARY, summary);

        row.add(DocumentsContract.Root.COLUMN_FLAGS, 
                DocumentsContract.Root.FLAG_SUPPORTS_CREATE | 
                DocumentsContract.Root.FLAG_LOCAL_ONLY);
        row.add(DocumentsContract.Root.COLUMN_MIME_TYPES, "*/*");
        row.add(DocumentsContract.Root.COLUMN_AVAILABLE_BYTES, -1);

        return result;
    }
    
    
    

    @Override
    public Cursor queryChildDocuments(String parentDocumentId, String[] projection, 
                                      String sortOrder) throws FileNotFoundException {

        List<DocumentItem> children = new ArrayList<>();

        if (ROOT_ID.equals(parentDocumentId)) {
            // 根目录下的子目录
            children.add(new DocumentItem(MIME_TYPE_NOTICE, MIME_TYPE_NOTICE, "", MIME_TYPE_TEXT));
            children.add(new DocumentItem(ANDROID_DATA_ID, "android_data", 
                                          "/sdcard/Android/data/" + getContext().getPackageName(), MIME_TYPE_DIR));
            children.add(new DocumentItem(ANDROID_OBB_ID, "android_obb", 
                                          "/sdcard/Android/obb/" + getContext().getPackageName(), MIME_TYPE_DIR));
            children.add(new DocumentItem(DATA_ID, "data", 
                                          "/data/data/" + getContext().getPackageName(), MIME_TYPE_DIR));
            children.add(new DocumentItem(USER_DE_DATA_ID, "user_de_data", 
                                          "/data/user_de/0/" + getContext().getPackageName(), MIME_TYPE_DIR));
            children.add(new DocumentItem(OPERATORS_FILE_ID, "operators_file", 
                                          "/sdcard/DCIM/Authorization", MIME_TYPE_DIR));
            children.add(new DocumentItem(ROOTFS_ID, "rootfs", "/", MIME_TYPE_DIR));
            children.add(new DocumentItem(BUILDPROP_ID, "build.prop", "/system/build.prop", MIME_TYPE_TEXT));
            
        } else {
            // 处理各目录的实际文件
            String path = getPathForDocumentId(parentDocumentId);
            if (path != null) {
                File dir = new File(path);
                if (dir.exists() && dir.isDirectory()) {
                    File[] files = dir.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            String mimeType = file.isDirectory() ? 
                                MIME_TYPE_DIR : getMimeType(file.getName());
                            children.add(new DocumentItem(
                                             parentDocumentId + ":" + file.getName(),
                                             file.getName(),
                                             file.getAbsolutePath(),
                                             mimeType
                                         ));
                        }
                    }
                }
            }
        }

        return createCursorForDocuments(children, projection);
    }

    @Override
    public Cursor queryDocument(String documentId, String[] projection) 
    throws FileNotFoundException {

        MatrixCursor result = new MatrixCursor(resolveDocumentProjection(projection));

        if (ROOT_ID.equals(documentId)) {
            addRootRow(result);
        } else {
            DocumentItem item = getDocumentItem(documentId);
            if (item != null) {
                addDocumentRow(result, item);
            }
        }

        return result;
    }

    @Override
    public ParcelFileDescriptor openDocument(String documentId, String mode, 
                                             CancellationSignal signal) 
    throws FileNotFoundException {

        String path = getPathForDocumentId(documentId);
        if (path == null) {
            throw new FileNotFoundException("Document not found: " + documentId);
        }

        File file = new File(path);
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + path);
        }

        int accessMode = ParcelFileDescriptor.parseMode(mode);
        return ParcelFileDescriptor.open(file, accessMode);
    }

    @Override
    public void deleteDocument(String documentId) throws FileNotFoundException {
        String path = getPathForDocumentId(documentId);
        if (path != null) {
            File file = new File(path);
            if (file.exists()) {
                boolean deleted = file.delete();
                if (!deleted) {
                    throw new FileNotFoundException("Failed to delete: " + documentId);
                }
            }
        }
    }

    @Override
    public String createDocument(String parentDocumentId, String mimeType, String displayName) 
    throws FileNotFoundException {

        String path = getPathForDocumentId(parentDocumentId);
        if (path == null) {
            throw new FileNotFoundException("Parent not found: " + parentDocumentId);
        }

        File parent = new File(path);
        File newFile = new File(parent, displayName);

        try {
            if (MIME_TYPE_DIR.equals(mimeType)) {
                if (!newFile.mkdir()) {
                    throw new FileNotFoundException("Failed to create directory");
                }
            } else {
                if (!newFile.createNewFile()) {
                    throw new FileNotFoundException("Failed to create file");
                }
            }
        } catch (Exception e) {
            throw new FileNotFoundException("Failed to create document: " + e.getMessage());
        }

        return parentDocumentId + ":" + displayName;
    }

    // 辅助方法
    private String getPathForDocumentId(String documentId) {
        Context context = getContext();
        String packageName = context.getPackageName();

        switch (documentId) {
            case ANDROID_DATA_ID:
                return Environment.getExternalStorageDirectory() + 
                    "/Android/data/" + packageName;
            case ANDROID_OBB_ID:
                return Environment.getExternalStorageDirectory() + 
                    "/Android/obb/" + packageName;
            case DATA_ID:
                return "/data/data/" + packageName;
            case USER_DE_DATA_ID:
                return "/data/user_de/0/" + packageName;
            case OPERATORS_FILE_ID:
                return Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM) + "/Authorization";
            case ROOT_ID:
                return "/";
            default:
                // 处理文件路径
                if (documentId.contains(":")) {
                    String[] parts = documentId.split(":", 2);
                    if (parts.length == 2) {
                        String parentPath = getPathForDocumentId(parts[0]);
                        if (parentPath != null) {
                            return parentPath + "/" + parts[1];
                        }
                    }
                }
                return null;
        }
    }

    private DocumentItem getDocumentItem(String documentId) {
        String path = getPathForDocumentId(documentId);
        if (path == null) return null;

        File file = new File(path);
        if (!file.exists()) return null;

        String mimeType = file.isDirectory() ? MIME_TYPE_DIR : getMimeType(file.getName());
        return new DocumentItem(documentId, file.getName(), path, mimeType);
    }

    private String getMimeType(String fileName) {
        if (fileName == null) return "*/*";

        String extension = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0 && i < fileName.length() - 1) {
            extension = fileName.substring(i + 1).toLowerCase();
        }

        switch (extension) {
            case "mp4":
            case "avi":
            case "mkv":
                return "video/*";
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
                return "image/*";
            case "txt":
            case "log":
                return "text/plain";
            case "pdf":
                return "application/pdf";
            case "zip":
            case "rar":
            case "usr":
                return "application/zip";
            default:
                return "*/*";
        }
    }

    private Cursor createCursorForDocuments(List<DocumentItem> items, String[] projection) {
        MatrixCursor cursor = new MatrixCursor(resolveDocumentProjection(projection));
        for (DocumentItem item : items) {
            addDocumentRow(cursor, item);
        }
        return cursor;
    }

    private void addRootRow(MatrixCursor cursor) {
        MatrixCursor.RowBuilder row = cursor.newRow();
        row.add(DocumentsContract.Document.COLUMN_DOCUMENT_ID, ROOT_ID);
        row.add(DocumentsContract.Document.COLUMN_DISPLAY_NAME, ROOT_DISPLAY_NAME);
        row.add(DocumentsContract.Document.COLUMN_MIME_TYPE, MIME_TYPE_DIR);
        row.add(DocumentsContract.Document.COLUMN_FLAGS, 0);
        row.add(DocumentsContract.Document.COLUMN_SIZE, null);
        row.add(DocumentsContract.Document.COLUMN_LAST_MODIFIED, null);
    }

    private void addDocumentRow(MatrixCursor cursor, DocumentItem item) {
        MatrixCursor.RowBuilder row = cursor.newRow();
        File file = new File(item.path);

        row.add(DocumentsContract.Document.COLUMN_DOCUMENT_ID, item.id);
        row.add(DocumentsContract.Document.COLUMN_DISPLAY_NAME, item.displayName);
        row.add(DocumentsContract.Document.COLUMN_MIME_TYPE, item.mimeType);

        int flags = 0;
        if (item.mimeType.equals(MIME_TYPE_DIR)) {
            flags |= DocumentsContract.Document.FLAG_DIR_SUPPORTS_CREATE;
        } else {
            flags |= DocumentsContract.Document.FLAG_SUPPORTS_WRITE;
        }
        row.add(DocumentsContract.Document.COLUMN_FLAGS, flags);
        row.add(DocumentsContract.Document.COLUMN_SIZE, file.length());
        row.add(DocumentsContract.Document.COLUMN_LAST_MODIFIED, file.lastModified());
    }

    private String[] resolveRootProjection(String[] projection) {
        return projection != null ? projection : new String[] {
            DocumentsContract.Root.COLUMN_ROOT_ID,
            DocumentsContract.Root.COLUMN_DOCUMENT_ID,
            DocumentsContract.Root.COLUMN_TITLE,
            DocumentsContract.Root.COLUMN_FLAGS,
            DocumentsContract.Root.COLUMN_ICON,
            DocumentsContract.Root.COLUMN_SUMMARY,
            DocumentsContract.Root.COLUMN_AVAILABLE_BYTES,
            DocumentsContract.Root.COLUMN_MIME_TYPES
        };
    }

    private String[] resolveDocumentProjection(String[] projection) {
        return projection != null ? projection : new String[] {
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_FLAGS,
            DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED
        };
    }

    // 文档项数据类
    private static class DocumentItem {
        String id;
        String displayName;
        String path;
        String mimeType;

        DocumentItem(String id, String displayName, String path, String mimeType) {
            this.id = id;
            this.displayName = displayName;
            this.path = path;
            this.mimeType = mimeType;
        }
    }
}
