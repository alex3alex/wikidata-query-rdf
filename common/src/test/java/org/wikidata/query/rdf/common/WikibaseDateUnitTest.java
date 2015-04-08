package org.wikidata.query.rdf.common;

import static org.wikidata.query.rdf.common.WikibaseDate.DAYS_PER_MONTH;
import static org.wikidata.query.rdf.common.WikibaseDate.fromSecondsSinceEpoch;
import static org.wikidata.query.rdf.common.WikibaseDate.fromString;
import static org.wikidata.query.rdf.common.WikibaseDate.isLeapYear;
import static org.wikidata.query.rdf.common.WikibaseDate.ToStringFormat.DATE;
import static org.wikidata.query.rdf.common.WikibaseDate.ToStringFormat.DATE_TIME;
import static org.wikidata.query.rdf.common.WikibaseDate.ToStringFormat.WIKIDATA;

import org.joda.time.chrono.GregorianChronology;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wikidata.query.rdf.common.WikibaseDate.ToStringFormat;

import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;

@RunWith(RandomizedRunner.class)
public class WikibaseDateUnitTest extends RandomizedTest {
    /**
     * Round trips epoch and explicitly tests some output and input formats.
     */
    @Test
    public void epoch() {
        WikibaseDate wbDate = check(1970, 1, 1, 0, 0, 0);
        assertEquals("+00000001970-01-01T00:00:00Z", wbDate.toString(ToStringFormat.WIKIDATA));
        assertEquals("1970-01-01T00:00:00Z", wbDate.toString(ToStringFormat.DATE_TIME));
        assertEquals("1970-01-01", wbDate.toString(ToStringFormat.DATE));
        assertEquals(wbDate, fromString("1970-1-1"));
        assertEquals(wbDate, fromString("1970-1-1T00:00"));
        assertEquals(wbDate, fromString("1970-1-1T00:00:00"));
        assertEquals(wbDate, fromString("1970-1-1T00:00:00Z"));
    }

    @Test
    public void yearOne() {
        check(1, 1, 1, 0, 0, 0);
    }

    @Test
    public void yearMinusOne() {
        check(-1, 1, 1, 0, 0, 0);
    }

    @Test
    public void yearZero() {
        check(0, 1, 1, 0, 0, 0);
    }

    @Test
    public void whenIWroteThis() {
        check(2015, 4, 1, 13, 53, 40);
    }

    @Test
    public void onLeapYear() {
        check(2000, 11, 1, 0, 0, 0);
    }

    @Test
    public void negativeLeapYear() {
        check(-4, 11, 1, 0, 0, 0);
    }

    @Test
    public void onLeapYearBeforeLeapDay() {
        check(2000, 2, 28, 13, 53, 40);
    }

    @Test
    public void onLeapYearOnLeapDay() {
        check(2000, 2, 29, 13, 53, 40);
    }

    @Test
    public void onLeapYearAfterLeapDay() {
        check(2000, 3, 1, 13, 53, 40);
    }

    @Test
    public void offLeapYearBeforeLeapDay() {
        check(2001, 2, 28, 13, 53, 40);
    }

    @Test
    public void offLeapYearAfterLeapDay() {
        check(2001, 3, 1, 13, 53, 40);
    }

    @Test
    public void veryNegativeYear() {
        check(-286893830, 1, 1, 0, 0, 0);
    }

    @Test
    public void bigBang() {
        WikibaseDate wbDate = fromString("-13798000000-00-00T00:00:00Z").cleanWeirdStuff();
        assertEquals(wbDate, fromString("-13798000000-01-01T00:00:00Z"));
        assertEquals(-435422885863219200L, wbDate.secondsSinceEpoch());
        checkRoundTrip(wbDate);
    }

    @Test
    @Repeat(iterations = 100)
    public void randomDate() {
        // Build a valid random date

        // Joda doesn't work outside these years
        int year = randomIntBetween(-292275054, 292278993);
        int month = randomIntBetween(1, 12);
        int day;
        if (isLeapYear(year) && month == 2) {
            day = randomIntBetween(1, 29);
        } else {
            day = randomIntBetween(1, DAYS_PER_MONTH[month - 1]);
        }
        int hour = randomIntBetween(0, 23);
        int minute = randomIntBetween(0, 59);
        int second = randomIntBetween(0, 59);
        check(year, month, day, hour, minute, second);
    }

    /**
     * Checks that the dates resolve the same way joda-time resolves dates and
     * that they round trip.
     */
    private WikibaseDate check(int year, int month, int day, int hour, int minute, int second) {
        WikibaseDate wbDate = new WikibaseDate(year, month, day, hour, minute, second);
        assertEquals(wbDate.toString(), jodaSeconds(year, month, day, hour, minute, second), wbDate.secondsSinceEpoch());
        checkRoundTrip(wbDate);
        return wbDate;
    }

    /**
     * Round trips the date through secondsSinceEpoch and all the toString and
     * fromString formats.
     */
    private void checkRoundTrip(WikibaseDate wbDate) {
        long seconds = wbDate.secondsSinceEpoch();
        WikibaseDate roundDate = fromSecondsSinceEpoch(seconds);
        assertEquals(wbDate, roundDate);
        long roundSeconds = roundDate.secondsSinceEpoch();
        assertEquals(seconds, roundSeconds);

        String string = wbDate.toString(WIKIDATA);
        roundDate = fromString(string);
        assertEquals(wbDate, roundDate);
        String roundString = roundDate.toString(WIKIDATA);
        assertEquals(string, roundString);

        string = wbDate.toString(DATE_TIME);
        roundDate = fromString(string);
        assertEquals(wbDate, roundDate);
        roundString = roundDate.toString(DATE_TIME);
        assertEquals(string, roundString);

        string = wbDate.toString(DATE);
        roundDate = fromString(string);
        if (wbDate.hour() == 0 && wbDate.minute() == 0 && wbDate.second() == 0) {
            assertEquals(wbDate, roundDate);
        }
        roundString = roundDate.toString(DATE);
        assertEquals(string, roundString);
    }

    /**
     * Get the seconds since epoch for a time according to Joda-Time.
     */
    private long jodaSeconds(int year, int month, int day, int hour, int minute, int second) {
        return GregorianChronology.getInstanceUTC().getDateTimeMillis(year, month, day, hour, minute, second, 0) / 1000;
    }
}