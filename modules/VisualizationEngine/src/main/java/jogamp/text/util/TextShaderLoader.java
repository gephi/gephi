package jogamp.text.util;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class TextShaderLoader
{

    static private String readStream(String filepath){
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream(filepath);
        assert is != null;
        BufferedInputStream bis = new BufferedInputStream(is);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        String data=null;
        try {
            for(int result = bis.read(); result != -1; result = bis.read()) {
                buf.write((byte) result);
                data= buf.toString(StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return data;

    }
    static public String getVertexShader(){
        return readStream("org/gephi/viz-engine/shaders/text/text.vert");
    }
    static public String getFragmentShader(){
       return readStream("org/gephi/viz-engine/shaders/text/text.frag");
    }
}
