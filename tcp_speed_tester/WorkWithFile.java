import java.io.File;
import java.io.IOException;

class WorkWithFile implements Constants {

    static String nameParser(String name) {
        byte[] buf = name.getBytes();
        int len = buf.length;
        int offset = len - 1;
        while (offset > 0 && buf[offset] != '/') {
            offset--;
        }
        if (0 == offset)
            return name;
        return new String(buf, offset, len - 1 - offset);
    }

    static File createFile(String nameOfFile) {
        File file = new File(nameOfFile);
        try {
            if (!file.createNewFile()) {
                if (!file.exists()) {
                    return null;
                }
                int i = 1;
                String newName = nameOfFile + String.valueOf(i);
                file = new File(newName);
                while (file.exists() && i < MAX_OF_SAME_FILES) {
                    ++i;
                    newName = nameOfFile + String.valueOf(i);
                    file = new File(newName);
                }
                if (i >= MAX_OF_SAME_FILES)
                    return null;
                if (!file.createNewFile())
                    return null;
                return file;
            }
            return file;
        } catch (IOException e) {
            return null;
        }
    }
}
