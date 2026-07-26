package org.cryptotrader.admin.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;
import org.cryptotrader.admin.component.DataPointFetcher;
import org.cryptotrader.admin.model.ChartDataPoint;
import org.cryptotrader.desktop.library.component.ComponentLoader;
import org.cryptotrader.desktop.library.component.config.SpringContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@Scope("prototype")
@Lazy
public class Chart extends HBox {
    private static final String AREA_CHART_STYLE_CLASS = "ct-area-chart";
    private static final String DEFAULT_CURRENCY_CODE = "BTC";

    @Autowired
    private DataPointFetcher dataPointFetcher;

    private List<ChartDataPoint<LocalDateTime, Double>> dataPoints;

    private final NumberAxis xAxis;
    private final NumberAxis yAxis;
    private final AreaChart<Number, Number> areaChart;
    private final XYChart.Series<Number, Number> series;


    public Chart() {
        SpringContext.getBean(ComponentLoader.class).loadWithFxRoot(this, this);
        this.xAxis = new NumberAxis();
        this.yAxis = new NumberAxis();

        this.xAxis.setForceZeroInRange(false);
        this.yAxis.setForceZeroInRange(false);

        this.xAxis.setAutoRanging(true);
        this.yAxis.setAutoRanging(true);

        this.xAxis.setTickLabelGap(6);
        this.yAxis.setTickLabelGap(6);

        // Format X as time "HH:mm:ss" (adjust formatter as desired)
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");
        this.xAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override public String toString(Number value) {
                long epochMillis = value.longValue();
                LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault());
                return timeFmt.format(ldt);
            }
            @Override public Number fromString(String string) { return 0L; }
        });

        // Chart
        this.areaChart = new AreaChart<>(xAxis, yAxis);
        this.areaChart.setAnimated(false);
        this.areaChart.setLegendVisible(false);
        this.areaChart.setCreateSymbols(false);
        this.areaChart.setHorizontalGridLinesVisible(true);
        this.areaChart.setVerticalGridLinesVisible(false);

        // Series
        this.series = new XYChart.Series<>();
        this.areaChart.getData().add(series);

        // Appearance
        this.areaChart.getStyleClass().add(AREA_CHART_STYLE_CLASS);
        this.areaChart.setEffect(new DropShadow());
        setPadding(new Insets(8));
        getChildren().add(this.areaChart);
        VBox.setVgrow(this.areaChart, Priority.ALWAYS);

        this.initGraph();
    }

    public void setDataPoints(List<ChartDataPoint<LocalDateTime, Double>> points) {
        this.dataPoints = points;
        ObservableList<XYChart.Data<Number, Number>> items = FXCollections.observableArrayList();
        if (points != null && !points.isEmpty()) {
            for (ChartDataPoint<LocalDateTime, Double> p : points) {
                long x = p.getX().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                Double y = p.getY();
                if (y != null) {
                    items.add(new XYChart.Data<>(x, y));
                }
            }
            this.series.setData(items);

            // Adjust axis range a bit for nicer margins
            long minX = items.stream().mapToLong(d -> d.getXValue().longValue()).min().orElse(0L);
            long maxX = items.stream().mapToLong(d -> d.getXValue().longValue()).max().orElse(0L);
            if (minX < maxX) {
                final double pad = (maxX - minX) * 0.05;
                this.xAxis.setAutoRanging(false);
                this.xAxis.setLowerBound(minX - pad);
                this.xAxis.setUpperBound(maxX + pad);
                this.xAxis.setTickUnit(Math.max(1, (maxX - minX) / 6.0));
            } else {
                this.xAxis.setAutoRanging(true);
            }

            final double minY = items.stream().mapToDouble(d -> d.getYValue().doubleValue()).min().orElse(0.0);
            final double maxY = items.stream().mapToDouble(d -> d.getYValue().doubleValue()).max().orElse(0.0);
            if (minY < maxY) {
                final double padY = (maxY - minY) * 0.1;
                this.yAxis.setAutoRanging(false);
                this.yAxis.setLowerBound(minY - padY);
                this.yAxis.setUpperBound(maxY + padY);
                this.yAxis.setTickUnit(Math.max(0.0001, (maxY - minY) / 5.0));
            } else {
                this.yAxis.setAutoRanging(true);
            }
        } else {
            series.getData().clear();
            xAxis.setAutoRanging(true);
            yAxis.setAutoRanging(true);
        }
    }

    public void initGraph() {
        final List<ChartDataPoint<LocalDateTime, Double>> dataPoints = this.dataPointFetcher.getLastDayCurrencyHistory(DEFAULT_CURRENCY_CODE);
        this.setDataPoints(dataPoints);
    }

    public Node getView() {
        return this;
    }

}
