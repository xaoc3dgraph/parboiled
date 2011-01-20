/*
 * Copyright (C) 2009-2010 Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.parboiled.buffers;

import org.parboiled.common.FileUtils;
import org.parboiled.errors.IllegalIndentationException;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.parboiled.buffers.InputBufferUtils.collectContent;
import static org.testng.Assert.assertEquals;

public class IndentDedentInputBufferTest {
    
    @Test
    public void testIndentDedentInputBuffer0() {
        InputBuffer buf = new IndentDedentInputBuffer(("" +
                "###\n" +
                "\n" +
                "L1#X\n" +
                "  \n" +
                "  L2\n" +
                "L12").toCharArray(), 2, "#");
        
        String bufContent = collectContent(buf);
        assertEquals(bufContent, "" +
                "L1\n" +
                "»L2\n" +
                "«L12");
        
        assertEquals(buf.extract(0, 2), "L1");
        assertEquals(buf.extract(4, 7), "L2\n");
        assertEquals(buf.extract(7, 11), "L12");
        assertEquals(buf.getPosition(5).toString(), "Position{line=5, column=4}");
        assertEquals(buf.extractLine(5), "  L2");        
    }

    @Test
    public void testIndentDedentInputBuffer1() {
        InputBuffer buf = new IndentDedentInputBuffer(("" +
                "level 1\n" +
                "  \tlevel 2\n" +
                "    still level 2\n" +
                "\t    level 3\n" +
                "      also 3\n" +
                "    2 again\n" +
                "        another 3\n" +
                "and back to 1\n" +
                "  another level 2 again").toCharArray(), 2, null);
        
        String bufContent = collectContent(buf);
        assertEquals(bufContent, "" +
                "level 1\n" +
                "»level 2\n" +
                "still level 2\n" +
                "»level 3\n" +
                "also 3\n" +
                "«2 again\n" +
                "»another 3\n" +
                "««and back to 1\n" +
                "»another level 2 again\n" +
                "«");
        
        String text = "another 3";
        int start = bufContent.indexOf(text);
        assertEquals(buf.extract(start, start + text.length()), text);  

        text = "back to 1";
        start = bufContent.indexOf(text);
        assertEquals(buf.extract(start, start + text.length()), text);
    }
    
    @Test
    public void testIndentDedentInputBuffer2() {
        String input = FileUtils.readAllTextFromResource("IndentDedentBuffer2.test");
        InputBuffer buf = new IndentDedentInputBuffer(input.toCharArray(), 4, "#");
        
        String bufContent = collectContent(buf);
        assertEquals(bufContent, FileUtils.readAllTextFromResource("IndentDedentBuffer2.converted.test"));
        
        String text = "go deep";
        int start = bufContent.indexOf(text);
        assertEquals(buf.extract(start, start + text.length()), text);        
    }

    @Test
    public void testIndentDedentInputBuffer3() {
        String input = FileUtils.readAllTextFromResource("IndentDedentBuffer3.test");
        InputBuffer buf = new IndentDedentInputBuffer(input.toCharArray(), 4, "//");
        String bufContent = collectContent(buf);
        assertEquals(bufContent, FileUtils.readAllTextFromResource("IndentDedentBuffer3.converted.test"));        
        assertEquals(buf.extract(0, bufContent.length()), input);
    }
    
    @Test
    public void testIndentDedentInputBuffeIllegalIndent() {
        try {
            new IndentDedentInputBuffer(("" +
                    "level 1\n" +
                    "  \tlevel 2\n" +
                    "    still level 2\n" +
                    "\t    level 3\n" +
                    "     illegal!!\n" +
                    "    2 again\n" +
                    "        another 3\n" +
                    "and back to 1\n" +
                    "  another level 2 again").toCharArray(), 2, null);
        } catch(IllegalIndentationException e) {
            assertEquals(e.getMessage(), "Illegal indentation in line 5:\n" +
            "     illegal!!\n" +
            "^^^^^\n");
            return;
        }
        Assert.fail("Incorrect or no IllegalIndentationException thrown");
    }
}
