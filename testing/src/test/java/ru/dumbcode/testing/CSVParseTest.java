package ru.dumbcode.testing;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CSVParseTest {
    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {     
                 { "#какой-то комментарий\nmark10,10\nmark1,5\n", "{mark1=[5], mark10=[10]}" },
                 { "#какой-то комментарий\nmarkFT,2.5\nmark2,9\n", "{}" }, 
                 { "#какой-то комментарий\nmark555,abc\nmarkFT,1\n", "{}" }, 
                 { "#какой-то комментарий\n#какой-то комментарий 2\nmarkABC,38", "{markABC=[38]}" },
                 { "#какой-то комментарий\nmarkABC,38\nmarkaBc,14", "{markABC=[14, 38]}" }
           });
    }

    private String fInput;

    private String fExpected;
    
    public CSVParseTest(String input, String expected) {
        this.fInput = input;
        this.fExpected = expected;
    }
    
    @Test
    public void test() {
        InputStream csvTest = new ByteArrayInputStream(fInput.getBytes());

        assertEquals(fExpected, Main.CSVParse(csvTest, null).toString());
    }

}
