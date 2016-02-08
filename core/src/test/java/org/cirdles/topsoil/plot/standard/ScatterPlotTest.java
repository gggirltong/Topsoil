/*
 * Copyright 2015 CIRDLES.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cirdles.topsoil.plot.standard;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.cirdles.topsoil.dataset.Dataset;
import org.cirdles.topsoil.dataset.RawData;
import org.cirdles.topsoil.dataset.SimpleDataset;
import org.cirdles.topsoil.plot.Plot;
import org.cirdles.topsoil.plot.PlotContext;
import org.cirdles.topsoil.plot.SimplePlotContext;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.testfx.framework.junit.ApplicationTest;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cirdles.topsoil.plot.standard.ScatterPlotProperties.TITLE;

/**
 *
 * @author John Zeringue
 */
public class ScatterPlotTest extends ApplicationTest {

    @Rule
    public Timeout timeout = Timeout.seconds(5);

    private Plot plot;

    @Override
    public void start(Stage stage) throws Exception {
        plot = new ScatterPlot();

        Scene scene = new Scene((Parent) plot.displayAsNode());
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void testGetVariables() {
        assertThat(plot.getVariables()).hasSize(2);
    }

    @Test
    public void testSetData() {
        RawData rawData = new RawData(new ArrayList<>(), new ArrayList<>());
        Dataset dataset = new SimpleDataset("Test", rawData);

        PlotContext plotContext
                = new SimplePlotContext(dataset);

        plot.setContext(plotContext);
    }

    @Test
    public void testProperties() throws Throwable {
        // check a default
        assertThat(plot.getProperty(TITLE)).isEqualTo("Scatter Plot");

        plot.setProperty(TITLE, "New Title");

        // check that it's changed
        assertThat(plot.getProperty(TITLE)).isEqualTo("New Title");
    }

}
