package org.gephi.ui.appearance.plugin;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class TransformerPanelUtils {
    static byte[] serialize(Object o) {
        try (
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(o);
            return bos.toByteArray();
        } catch (
            IOException e) {
            throw new RuntimeException(e);
        }
    }
    static Object deserialize(byte[] bytes) {
        if (bytes != null) {
            try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                 ObjectInputStream ois = new ObjectInputStream(bis)) {
                  return ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
