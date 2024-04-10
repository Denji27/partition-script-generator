package org.example;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PartitionGenerator partitionGenerator = new PartitionGenerator();

            partitionGenerator.pack();

            partitionGenerator.setLocation(
                    (Toolkit.getDefaultToolkit().getScreenSize().width) / 2 - partitionGenerator.getWidth() + 5 / 2,
                    (Toolkit.getDefaultToolkit().getScreenSize().height) / 2 - partitionGenerator.getHeight() + 5 / 2);

            partitionGenerator.setVisible(true);
        });
    }
}
