import org.gephi.appearance.plugin.palette.PaletteManager;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class PaletteTest {
    @Test
    public void testPalette(){
        PaletteManager paletteManager = PaletteManager.getInstance();

        // input parameter, expected result
        Map<Integer,Integer> testCases = new HashMap<>();
        testCases.put(400,2);
        testCases.put(300,5);
        testCases.put(250,5);
        testCases.put(200,10);
        testCases.put(150,10);
        testCases.put(100,25);
        testCases.put(75,25);
        testCases.put(50,50);
        testCases.put(10,50);

        for(Map.Entry<Integer,Integer> test:testCases.entrySet()){
            Integer result =  paletteManager.getGeneratePaletteQuality(test.getKey());
            Assert.assertEquals(test.getValue(), result);
        }
    }
}
