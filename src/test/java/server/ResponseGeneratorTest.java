package server;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;

public class ResponseGeneratorTest{

    @Test
    public void testZeroLengthString() {
        String request = "";

        ResponseGenerator generator = new ResponseGenerator(request);

        assertNotNull(generator);

        assertNotNull(generator.response());
        assertEquals("명령을 입력해주세요.\r\n",generator.response());
        assertFalse(generator.isClose());
    }

}
