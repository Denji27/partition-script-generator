package org.example;


import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PartitionGenerator extends JDialog {
    private JPanel contentPane;
    private JButton btnCancel;
    private JButton btnGenerate;
    private JTextField txtTableName;
    private JTextArea txtResult;
    private JComboBox partitionType;
    private JTextArea valueOfLists;
    private JButton exitButton;
    private JScrollPane resultScrollArea;
    private JScrollPane inputScollArea;
    private JLabel listLabel;
    private JTextField textFromDate;
    private JTextField textToDate;
    private JLabel fromLabel;
    private JLabel toLabel;
    private JComboBox textDataType;
    private JLabel dataTypeLabel;

    public PartitionGenerator() {
        setContentPane(contentPane);
//        setModal(true);
        setTitle("Partition script generator");
        getRootPane().setDefaultButton(btnCancel);
        btnCancel.addActionListener(e -> onRefresh());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        partitionType.addActionListener(e -> onSelect());
        listLabel.setVisible(Boolean.TRUE);
        valueOfLists.setVisible(Boolean.TRUE);
        dataTypeLabel.setVisible(Boolean.FALSE);
        textDataType.setVisible(Boolean.FALSE);
        fromLabel.setVisible(Boolean.FALSE);
        toLabel.setVisible(Boolean.FALSE);
        textFromDate.setVisible(Boolean.FALSE);
        textToDate.setVisible(Boolean.FALSE);
        addWindowListener(
                new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        onExit();
                    }
                });
        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(
                e -> onExit(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        btnGenerate.addActionListener(e -> onGenerate());
        exitButton.addActionListener(e -> onExit());
        txtTableName.addKeyListener(
                new KeyAdapter() {
                    /**
                     * Invoked when a key has been pressed.
                     *
                     * @param e KeyEvent
                     */
                    @Override
                    public void keyPressed(KeyEvent e) {
                        txtTableName.setText(txtTableName.getText().replaceAll("\\s+", ""));
                    }
                });

        valueOfLists.addKeyListener(
                new KeyAdapter() {
                    /**
                     * Invoked when a key has been pressed.
                     *
                     * @param e KeyEvent
                     */
                    @Override
                    public void keyPressed(KeyEvent e) {
                        valueOfLists.setText(valueOfLists.getText());
                    }
                });
    }

    private void onRefresh() {
        // add your code here if necessary
        valueOfLists.setText("");
        txtTableName.setText("");
        txtResult.setText("");
        textFromDate.setText("");
        textToDate.setText("");
//        dispose();
    }

    private void onExit(){
        dispose();
    }

    private void onGenerate() {
        String tableName = txtTableName.getText();
        PartitionType type = null;
        if (Objects.requireNonNull(this.partitionType.getSelectedItem()).toString().equals("LIST")) {
            type = PartitionType.LIST;
            String rawValues = valueOfLists.getText();
            StringBuilder result = new StringBuilder();
            String[] vals = rawValues.split("\n");
            for (int i = 0; i < vals.length; i++) {
                List<String> elements = Arrays.stream(vals[i].trim().split(",\\s*")).collect(Collectors.toList());
                ListPartitionRequirement requirement =
                        ListPartitionRequirement.builder()
                                .values(elements)
                                .tableName(tableName)
                                .partitionType(type)
                                .build();
                result.append(generateListPartitionScript(requirement, i));
            }
            txtResult.setText(result.toString());
        }
        if (Objects.requireNonNull(this.partitionType.getSelectedItem()).toString().equals("RANGE")){
            type = PartitionType.RANGE;
            DataType dataType = null;
            if (Objects.requireNonNull(textDataType.getSelectedItem()).toString().equals("DAY")) {
                dataType = DataType.DAY;
            }
            if (Objects.requireNonNull(textDataType.getSelectedItem()).toString().equals("MONTH")) {
                dataType = DataType.MONTH;
            }
            if (Objects.requireNonNull(textDataType.getSelectedItem()).toString().equals("YEAR")) {
                dataType = DataType.YEAR;
            }
            LocalDate from = LocalDate.parse(textFromDate.getText());
            LocalDate to = LocalDate.parse(textToDate.getText());

            RangePartitionRequirement requirement =
                    RangePartitionRequirement.builder()
                            .dataType(dataType)
                            .from(from)
                            .to(to)
                            .partitionType(type)
                            .tableName(tableName)
                            .build();
            txtResult.setText(generateRangePartitionScript(requirement));
        }
    }

    private void onSelect(){
        if (Objects.requireNonNull(this.partitionType.getSelectedItem()).toString().equals("LIST")){
            listLabel.setVisible(Boolean.TRUE);
            valueOfLists.setVisible(Boolean.TRUE);
            inputScollArea.setVisible(Boolean.TRUE);
            dataTypeLabel.setVisible(Boolean.FALSE);
            textDataType.setVisible(Boolean.FALSE);
            fromLabel.setVisible(Boolean.FALSE);
            toLabel.setVisible(Boolean.FALSE);
            textFromDate.setVisible(Boolean.FALSE);
            textToDate.setVisible(Boolean.FALSE);
        }
        if (Objects.requireNonNull(this.partitionType.getSelectedItem()).toString().equals("RANGE")){
            listLabel.setVisible(Boolean.FALSE);
            valueOfLists.setVisible(Boolean.FALSE);
            inputScollArea.setVisible(Boolean.FALSE);
            dataTypeLabel.setVisible(Boolean.TRUE);
            textDataType.setVisible(Boolean.TRUE);
            fromLabel.setVisible(Boolean.TRUE);
            toLabel.setVisible(Boolean.TRUE);
            textFromDate.setVisible(Boolean.TRUE);
            textToDate.setVisible(Boolean.TRUE);
        }
    }

    public static String generateListPartitionScript(
            ListPartitionRequirement requirement, int i) {
        StringBuilder script = new StringBuilder();
        script.append("CREATE TABLE ");
        script.append(createPartitionTableName(requirement, i));
        script.append(" PARTITION OF ");
        script.append(requirement.getTableName());
        script.append(" FOR VALUES IN (");
        for (String value : requirement.getValues()) {
            script.append("'").append(value).append("', ");
        }
        script.delete(script.length() - 2, script.length() - 1);
        script.append(")");
        script.append("; \n");
        return script.toString();
    }

    private static String createPartitionTableName(
            ListPartitionRequirement requirement, int number) {
        return requirement.getTableName() + "_" + number;
    }

    public static String generateRangePartitionScript(
            RangePartitionRequirement requirement) {
        StringBuilder script = new StringBuilder();
        LocalDate from = requirement.getFrom();
        LocalDate to = requirement.getTo();
        if (requirement.getDataType().equals(DataType.DAY)) {
            long b;
            if (!from.isBefore(to)) {
                return null;
            }
            b = ChronoUnit.DAYS.between(from, to);
            for (int i = 0; i <= b; i++) {
                LocalDate f = from;
                LocalDate t = from.plusDays(1);
                RangePartitionRequirement rangePartitionRequirement =
                        RangePartitionRequirement.builder()
                                .dataType(DataType.DAY)
                                .from(f)
                                .to(t)
                                .partitionType(PartitionType.RANGE)
                                .tableName(requirement.getTableName())
                                .build();
                from = t;
                script.append(genSingleScript(rangePartitionRequirement));
                script.append("; \n");
            }
        }

        if (requirement.getDataType().equals(DataType.MONTH)) {
            LocalDate firstOfFrom = from.withDayOfMonth(1);
            LocalDate lastOfTo = to.withDayOfMonth(1);
            long monthsBetween = ChronoUnit.MONTHS.between(firstOfFrom, lastOfTo);
            for (int i = 0; i <= monthsBetween; i++) {
                LocalDate f = from.withDayOfMonth(1);
                LocalDate t = f.plusMonths(1);
                RangePartitionRequirement rangePartitionRequirement =
                        RangePartitionRequirement.builder()
                                .dataType(DataType.MONTH)
                                .from(f)
                                .to(t)
                                .partitionType(PartitionType.RANGE)
                                .tableName(requirement.getTableName())
                                .build();
                from = t;
                script.append(genSingleScript(rangePartitionRequirement));
                script.append("; \n");
            }
        }

        if (requirement.getDataType().equals(DataType.YEAR)) {
            LocalDate firstOfFrom = from.withDayOfYear(1);
            LocalDate lastOfTo = to.withDayOfYear(1);
            long yearsBetween = ChronoUnit.YEARS.between(firstOfFrom, lastOfTo);
            for (int i = 0; i <= yearsBetween; i++) {
                LocalDate f = from.withDayOfYear(1);
                LocalDate t = f.plusYears(1);
                RangePartitionRequirement rangePartitionRequirement =
                        RangePartitionRequirement.builder()
                                .dataType(DataType.YEAR)
                                .from(f)
                                .to(t)
                                .partitionType(PartitionType.RANGE)
                                .tableName(requirement.getTableName())
                                .build();
                from = t;
                script.append(genSingleScript(rangePartitionRequirement));
                script.append("; \n");
            }
        }
        return script.toString();
    }

    public static String genSingleScript(RangePartitionRequirement requirement) {
        StringBuilder script = new StringBuilder();
        script.append("CREATE TABLE ");
        script.append(createRangePartitionTableName(requirement));
        script.append(" PARTITION OF ");
        script.append(requirement.getTableName());
        script.append(" FOR VALUES FROM ('");
        script.append(requirement.getFrom().toString());
        script.append("') TO ('");
        script.append(requirement.getTo().toString());
        script.append("')");
        return script.toString();
    }

    private static String createRangePartitionTableName(
            RangePartitionRequirement requirement) {
        StringBuilder tableName = new StringBuilder();
        tableName.append(requirement.getTableName());
        tableName.append("_p");
        if (Objects.equals(
                requirement.getDataType(),
                DataType.YEAR)
                && Objects.nonNull(requirement.getFrom())
                && Objects.nonNull(requirement.getTo())) {
            tableName.append(requirement.getFrom().getYear());
        }
        if (Objects.equals(
                requirement.getDataType(),
                DataType.MONTH)
                && Objects.nonNull(requirement.getFrom())
                && Objects.nonNull(requirement.getTo())) {
            tableName.append(requirement.getFrom().getYear());
            tableName.append(
                    String.format("%02d", requirement.getFrom().getMonthValue()));
        }
        if (Objects.equals(
                requirement.getDataType(),
                DataType.DAY)
                && Objects.nonNull(requirement.getFrom())
                && Objects.nonNull(requirement.getTo())) {
            tableName.append(requirement.getFrom().getYear());
            tableName.append(
                    String.format("%02d", requirement.getFrom().getMonthValue()));
            tableName.append(
                    String.format("%02d", requirement.getFrom().getDayOfMonth()));
        }
        return tableName.toString();
    }

}
