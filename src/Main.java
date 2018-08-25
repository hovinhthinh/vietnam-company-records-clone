
import util.Miscellaneous;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import site.Thongtincongty;
import util.CollaborationQueue;
import util.Crawler;
import util.TParser;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author hovinhthinh
 */
public class Main extends javax.swing.JFrame {

    private File output;

    /**
     * Creates new form EmailChecker
     */
    public Main() {
        initComponents();

        dataTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                if (dataTable.getSelectedRow() > -1) {
                    // TODO: Something could be done here.
                }
            }
        });

        searchLinkTextField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateFieldState();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateFieldState();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateFieldState();
            }

            protected void updateFieldState() {
                checkStateEnabled();
            }
        });
    }

    private void checkStateEnabled() {
        boolean enabled = searchLinkTextField.getText().startsWith("http://www.thongtincongty.com/") && output != null;
        startButton.setEnabled(enabled);
    }

    public void log(String info) {
        systemLogTextArea.append(info + "\r\n");
    }

    private boolean outputFileIsValid(File outputFile) {
        if (outputFile.exists()) {
            int result = JOptionPane.showConfirmDialog(
                    outputFileButton.getParent(),
                    "File exists, overwrite?", "File exists",
                    JOptionPane.YES_NO_CANCEL_OPTION);
            switch (result) {
                case JOptionPane.YES_OPTION:
                    return true;
                default:
                    return false;
            }
        }
        return true;
    }

    public synchronized void addExportedRecord(Thongtincongty e) {
        DefaultTableModel tableModel = (DefaultTableModel) dataTable.getModel();
        tableModel.addRow(e.toTSVLine().split("\t"));
    }

    private void resetGUI() {
        output = null;
        searchLinkTextField.setEnabled(true);
        outputFileLabel.setText("No chosen file");
        outputFileButton.setEnabled(true);
        useProxy.setEnabled(true);
        numberOfWorkersComboBox.setEnabled(true);

        startButton.setText("Start");
        startButton.setEnabled(false);
    }

    private boolean export() {
        int nWorkers = Integer.parseInt(numberOfWorkersComboBox.getSelectedItem().toString());
        CollaborationQueue<URL> queue = new CollaborationQueue<>(nWorkers);

        final DefaultTableModel tableModel = (DefaultTableModel) dataTable.getModel();
        tableModel.setRowCount(0);
        String url = searchLinkTextField.getText();

        int numRecords;
        try {
            String data = Crawler.getContentFromUrl(url);
            numRecords = Integer.parseInt(TParser.getContent(data,
                    "<h4>Có ", " doanh nghiệp phù hợp với "));
        } catch (NumberFormatException e) {
            numRecords = -1;
        }

        final int totalRecords = numRecords;

        if (totalRecords == -1) {
            log("Cannot retrieve information.");
            resetGUI();
            return false;
        }
        log("Retrieving total " + totalRecords + " records");
        log("Writing result using TSV format.");

        try {
            PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"));
            out.println(Thongtincongty.getTSVHeader());
            AtomicInteger pagesCount = new AtomicInteger(0);
            
            for (int i = 1; i <= (totalRecords - 1) / 50 + 1; ++i) {
                queue.push(new URL(url + "?page=" + i));
            } 

            ArrayList<Thread> threads = new ArrayList<>();

            for (int i = 0; i < Integer.parseInt(numberOfWorkersComboBox.getSelectedItem().toString()); ++i) {
                threads.add(new Worker(queue, out, pagesCount, useProxy.isSelected(), this));
            }

            // Monitor.
            threads.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    log("Monitor started.");
                    int lastPagesCount = 0;
                    LinkedList<Double> speedLog = new LinkedList<>();
                    double sumSpeed = 0;
                    try {
                        int pageCount;
                        do {
                            Thread.sleep(10000);
                            pageCount = pagesCount.get();
                            double speed = (double) (pageCount - lastPagesCount) / 10;
                            lastPagesCount = pageCount;

                            String logString = String.format("Crawling_speed: %.1f pages/s               Exported_records: %d", speed, tableModel.getRowCount());
                            sumSpeed += speed;
                            speedLog.add(speed);
                            if (speedLog.size() > 10) {
                                sumSpeed -= speedLog.removeFirst();
                            }

                            int etr = (Math.abs(sumSpeed) < 1e-3) ? -1
                                    : (int) ((totalRecords - tableModel.getRowCount()) / (sumSpeed / speedLog.size()));
                            if (etr != -1) {
                                logString += String.format("               ETR: %02d:%02d:%02d", etr / 3600, (etr % 3600) / 60, etr % 60);
                            } else {
                                logString += String.format("               ETR: --:--:--");
                            }
                            progressLabel.setText(logString);
                            if (queue.isEnded) {
                                log("Monitor stopped.");
                                log("Done.");
                                log("Output: " + output.getAbsolutePath());
                                resetGUI();
                            }
                        } while (!queue.isEnded);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }));

            for (Thread t : threads) {
                t.start();
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        outputFileButton = new javax.swing.JButton();
        searchLinkLabel = new javax.swing.JLabel();
        outputFileLabel = new javax.swing.JLabel();
        useProxy = new javax.swing.JCheckBox();
        numberOfWorkersComboBox = new javax.swing.JComboBox();
        numberOfWorkersLabel = new javax.swing.JLabel();
        dataPanel = new javax.swing.JScrollPane();
        dataTable = new javax.swing.JTable();
        progressLabel = new javax.swing.JLabel();
        startButton = new javax.swing.JButton();
        logPanel = new javax.swing.JScrollPane();
        logTextArea = new javax.swing.JTextArea();
        systemLogLabel = new javax.swing.JLabel();
        systemLogPanel = new javax.swing.JScrollPane();
        systemLogTextArea = new javax.swing.JTextArea();
        checkLogLabel = new javax.swing.JLabel();
        searchLinkTextField = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("thongtincongty-export");

        outputFileButton.setText("Choose Output File");
        outputFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outputFileButtonActionPerformed(evt);
            }
        });

        searchLinkLabel.setText("Search link: ");

        outputFileLabel.setText("No chosen file");

        useProxy.setText("Use proxy (Not implemented)");
        useProxy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useProxyActionPerformed(evt);
            }
        });

        numberOfWorkersComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "4", "8", "16", "32", "64", "128" }));
        numberOfWorkersComboBox.setSelectedIndex(4);
        numberOfWorkersComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                numberOfWorkersComboBoxActionPerformed(evt);
            }
        });

        numberOfWorkersLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        numberOfWorkersLabel.setText("Number of threads");

        dataTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Công ty", "Tên giao dịch", "Mã số thuế", "Địa chỉ", "Đại diện pháp luật", "Ngày cấp giấy phép", "Ngày hoạt động", "Điện thoại", "Ngành nghề kinh doanh"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        dataTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        dataPanel.setViewportView(dataTable);

        progressLabel.setText("Crawling_speed: -- pages/s               Exported_records: --               ETR: --:--:--");

        startButton.setFont(new java.awt.Font("Lucida Grande", 0, 36)); // NOI18N
        startButton.setText("Start");
        startButton.setEnabled(false);
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });

        logTextArea.setEditable(false);
        logTextArea.setColumns(20);
        logTextArea.setRows(5);
        logPanel.setViewportView(logTextArea);

        systemLogLabel.setText("System log");

        systemLogTextArea.setEditable(false);
        systemLogTextArea.setColumns(20);
        systemLogTextArea.setRows(5);
        systemLogPanel.setViewportView(systemLogTextArea);

        checkLogLabel.setText("Check log");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(progressLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(dataPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 565, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(outputFileButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(outputFileLabel)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(searchLinkLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(searchLinkTextField)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(checkLogLabel)
                            .addComponent(logPanel, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(startButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addComponent(numberOfWorkersLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(numberOfWorkersComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(useProxy, javax.swing.GroupLayout.Alignment.TRAILING))
                            .addComponent(systemLogPanel)
                            .addComponent(systemLogLabel))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchLinkLabel)
                    .addComponent(numberOfWorkersComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(numberOfWorkersLabel)
                    .addComponent(searchLinkTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(outputFileButton)
                    .addComponent(outputFileLabel)
                    .addComponent(useProxy))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(systemLogLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(systemLogPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(checkLogLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(logPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 297, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(startButton))
                    .addComponent(dataPanel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressLabel)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void outputFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outputFileButtonActionPerformed
        final JFileChooser fc = new JFileChooser();

        fc.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                String extension = Miscellaneous.getFileExtension(f);
                if (extension != null) {
                    return extension.equals(Miscellaneous.TSV);
                }
                return false;
            }

            @Override
            public String getDescription() {
                return "TSV";
            }
        });

        fc.setAcceptAllFileFilterUsed(false);

        int returnVal = fc.showSaveDialog(Main.this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            String extension = Miscellaneous.getFileExtension(file);
            if (extension == null || !extension.equals(Miscellaneous.TSV)) {
                file = new File(file.toString() + "." + Miscellaneous.TSV);
            }
            if (outputFileIsValid(file)) {
                output = file;
                String outputName = output.getAbsolutePath();
                if (outputName.length() > 50) {
                    outputFileLabel.setText("..." + outputName.substring(outputName.length() - 47));
                } else {
                    outputFileLabel.setText(outputName);
                }
                checkStateEnabled();
            }
        }
    }//GEN-LAST:event_outputFileButtonActionPerformed

    private void useProxyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useProxyActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_useProxyActionPerformed

    private void numberOfWorkersComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_numberOfWorkersComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_numberOfWorkersComboBoxActionPerformed

    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startButtonActionPerformed
        outputFileButton.setEnabled(false);
        searchLinkTextField.setEnabled(false);
        useProxy.setEnabled(false);
        numberOfWorkersComboBox.setEnabled(false);

        startButton.setText("Exporting...");
        startButton.setEnabled(false);

        export();
    }//GEN-LAST:event_startButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Main.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Main.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Main.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Main.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Main().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel checkLogLabel;
    private javax.swing.JScrollPane dataPanel;
    private javax.swing.JTable dataTable;
    private javax.swing.JScrollPane logPanel;
    private javax.swing.JTextArea logTextArea;
    private javax.swing.JComboBox numberOfWorkersComboBox;
    private javax.swing.JLabel numberOfWorkersLabel;
    private javax.swing.JButton outputFileButton;
    private javax.swing.JLabel outputFileLabel;
    private javax.swing.JLabel progressLabel;
    private javax.swing.JLabel searchLinkLabel;
    private javax.swing.JTextField searchLinkTextField;
    private javax.swing.JButton startButton;
    private javax.swing.JLabel systemLogLabel;
    private javax.swing.JScrollPane systemLogPanel;
    private javax.swing.JTextArea systemLogTextArea;
    private javax.swing.JCheckBox useProxy;
    // End of variables declaration//GEN-END:variables
}
