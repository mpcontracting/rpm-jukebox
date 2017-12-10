package uk.co.mpcontracting.rpmjukebox.component;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;

public class PercentageTableColumnTest extends AbstractTest {

    @Test
    public void shouldGetPercentageWidthProperty() {
        PercentageTableColumn<String, String> percentageTableColumn = new PercentageTableColumn<>();
        
        assertThat("Percentage width should not be null", percentageTableColumn.percentageWidthProperty(), notNullValue());
    }
    
    @Test
    public void shouldGetPercentageWidth() {
        PercentageTableColumn<String, String> percentageTableColumn = new PercentageTableColumn<>();
        
        assertThat("Percentage width should be 1.0", percentageTableColumn.getPercentageWidth(), equalTo(1.0d));
    }
    
    @Test
    public void shouldSetPercentageWidth() {
        PercentageTableColumn<String, String> percentageTableColumn = new PercentageTableColumn<>();
        percentageTableColumn.setPercentageWidth(0.5);
        
        assertThat("Percentage width should be 0.5", percentageTableColumn.getPercentageWidth(), equalTo(0.5d));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToSetPercentageWidthLessThanZero() {
        PercentageTableColumn<String, String> percentageTableColumn = new PercentageTableColumn<>();
        percentageTableColumn.setPercentageWidth(-0.1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToSetPercentageWidthGreaterThanOne() {
        PercentageTableColumn<String, String> percentageTableColumn = new PercentageTableColumn<>();
        percentageTableColumn.setPercentageWidth(1.1);
    }
}
