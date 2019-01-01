package us.mrvivacio.porno;

import org.junit.Test;

import static org.junit.Assert.*;

public class porNoTest {

    @Test
    public void isPorn() {
        boolean malformedURL = porNo.isPorn("pornhub com");
        boolean isPornLongURL = porNo.isPorn("bangbros.com/here&is&some&filler&text/lmao.html");

        assertFalse(malformedURL);
        assertTrue(isPornLongURL);
    }

    @Test
    public void isNotPorn() {
        boolean notPornURL = porNo.isPorn("https://chrome.google.com/webstore/detail/porno-beta/fkhfpbfakkjpkhnonhelnnbohblaeooj");

        assertFalse(notPornURL);
    }
}