/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * EditorView.java
 *
 * Created on Jul 8, 2010, 1:56:17 PM
 */

package carneades.editor.uicomponents;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

/**
 *
 * @author pal
 */
public class EditorApplicationView extends javax.swing.JFrame {

    /** Creates new form EditorView */
    public EditorApplicationView() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        leftTabbedPane = new javax.swing.JTabbedPane();
        filesPane = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        searchPanel = new javax.swing.JPanel();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        editMenu = new javax.swing.JMenu();
        cutMenuItem = new javax.swing.JMenuItem();
        copyMenuItem = new javax.swing.JMenuItem();
        pasteMenuItem = new javax.swing.JMenuItem();
        deleteMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        contentsMenuItem = new javax.swing.JMenuItem();

        tabPopupMenu.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        closeTabMenuItem.setText("Close");
        tabPopupMenu.add(closeTabMenuItem);

        lkifFilePopupMenu.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        closeLkifFileMenuItem.setText("Close");
        lkifFilePopupMenu.add(closeLkifFileMenuItem);
        lkifFilePopupMenu.add(jSeparator5);

        exportLkifFileMenuItem.setText("Export...");
        lkifFilePopupMenu.add(exportLkifFileMenuItem);

        graphPopupMenu.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        openGraphMenuItem.setText("Open");
        graphPopupMenu.add(openGraphMenuItem);

        closeGraphMenuItem.setText("Close");
        graphPopupMenu.add(closeGraphMenuItem);
        graphPopupMenu.add(jSeparator6);

        exportGraphMenuItem.setText("Export...");
        graphPopupMenu.add(exportGraphMenuItem);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        mainPanel.setOneTouchExpandable(true);

        leftPanel.setPreferredSize(new java.awt.Dimension(300, 10));
        leftPanel.setLayout(new javax.swing.BoxLayout(leftPanel, javax.swing.BoxLayout.LINE_AXIS));

        filesPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jPanel1.setPreferredSize(new java.awt.Dimension(306, 200));

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        lkifsTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        lkifsTree.setMaximumSize(new java.awt.Dimension(32767, 32767));
        jScrollPane2.setViewportView(lkifsTree);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
                .addContainerGap())
        );

        filesPane.setLeftComponent(jPanel1);

        propertiesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Properties"));
        propertiesPanel.setLayout(new javax.swing.BoxLayout(propertiesPanel, javax.swing.BoxLayout.PAGE_AXIS));
        filesPane.setRightComponent(propertiesPanel);

        leftTabbedPane.addTab("Files", filesPane);

        javax.swing.GroupLayout searchPanelLayout = new javax.swing.GroupLayout(searchPanel);
        searchPanel.setLayout(searchPanelLayout);
        searchPanelLayout.setHorizontalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 296, Short.MAX_VALUE)
        );
        searchPanelLayout.setVerticalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 511, Short.MAX_VALUE)
        );

        leftTabbedPane.addTab("Search", searchPanel);

        leftPanel.add(leftTabbedPane);

        mainPanel.setLeftComponent(leftPanel);
        mainPanel.setRightComponent(mapPanel);

        toolBar.setFloatable(false);
        toolBar.setRollover(true);

        openFileButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/document-open.png"))); // NOI18N
        openFileButton.setFocusable(false);
        openFileButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        openFileButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(openFileButton);

        saveFileButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/document-save.png"))); // NOI18N
        saveFileButton.setFocusable(false);
        saveFileButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        saveFileButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(saveFileButton);
        toolBar.add(jSeparator4);

        zoomResetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/zoomreset.png"))); // NOI18N
        zoomResetButton.setBorder(null);
        zoomResetButton.setBorderPainted(false);
        zoomResetButton.setFocusPainted(false);
        zoomResetButton.setFocusable(false);
        zoomResetButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        zoomResetButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(zoomResetButton);

        zoomOutButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/zoomout.png"))); // NOI18N
        zoomOutButton.setBorder(null);
        zoomOutButton.setFocusable(false);
        zoomOutButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        zoomOutButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(zoomOutButton);

        zoomInButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/zoomin.png"))); // NOI18N
        zoomInButton.setBorder(null);
        zoomInButton.setFocusable(false);
        zoomInButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        zoomInButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(zoomInButton);

        fileMenu.setText("File");

        openFileMenuItem.setText("Open...");
        fileMenu.add(openFileMenuItem);

        closeFileMenuItem.setText("Close");
        fileMenu.add(closeFileMenuItem);
        fileMenu.add(jSeparator2);

        saveFileMenuItem.setText("Save");
        fileMenu.add(saveFileMenuItem);

        saveAsFileMenuItem.setText("Save As...");
        fileMenu.add(saveAsFileMenuItem);
        fileMenu.add(jSeparator3);

        exportFileMenuItem.setText("Export...");
        fileMenu.add(exportFileMenuItem);
        fileMenu.add(jSeparator7);

        printPreviewFileMenuItem.setText("Print Preview");
        fileMenu.add(printPreviewFileMenuItem);

        printMenuItem.setText("Print...");
        fileMenu.add(printMenuItem);
        fileMenu.add(jSeparator1);

        exitFileMenuItem.setText("Exit");
        exitFileMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitFileMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitFileMenuItem);

        menuBar.add(fileMenu);

        editMenu.setText("Edit");

        cutMenuItem.setText("Cut");
        editMenu.add(cutMenuItem);

        copyMenuItem.setText("Copy");
        editMenu.add(copyMenuItem);

        pasteMenuItem.setText("Paste");
        editMenu.add(pasteMenuItem);

        deleteMenuItem.setText("Delete");
        editMenu.add(deleteMenuItem);

        menuBar.add(editMenu);

        helpMenu.setText("Help");

        contentsMenuItem.setText("Contents");
        helpMenu.add(contentsMenuItem);

        aboutHelpMenuItem.setText("About");
        helpMenu.add(aboutHelpMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 821, Short.MAX_VALUE)
            .addComponent(toolBar, javax.swing.GroupLayout.DEFAULT_SIZE, 821, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 542, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exitFileMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitFileMenuItemActionPerformed
        System.exit(0);
    }//GEN-LAST:event_exitFileMenuItemActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new EditorApplicationView().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public final javax.swing.JMenuItem aboutHelpMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JMenuItem closeFileMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JMenuItem closeGraphMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JMenuItem closeLkifFileMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JMenuItem closeTabMenuItem = new javax.swing.JMenuItem();
    private javax.swing.JMenuItem contentsMenuItem;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JMenuItem deleteMenuItem;
    private javax.swing.JMenu editMenu;
    public final javax.swing.JMenuItem exitFileMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JMenuItem exportFileMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JMenuItem exportGraphMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JMenuItem exportLkifFileMenuItem = new javax.swing.JMenuItem();
    private javax.swing.JMenu fileMenu;
    private javax.swing.JSplitPane filesPane;
    public final javax.swing.JPopupMenu graphPopupMenu = new javax.swing.JPopupMenu();
    private javax.swing.JMenu helpMenu;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator7;
    public final javax.swing.JPanel leftPanel = new javax.swing.JPanel();
    private javax.swing.JTabbedPane leftTabbedPane;
    public final javax.swing.JPopupMenu lkifFilePopupMenu = new javax.swing.JPopupMenu();
    public final javax.swing.JTree lkifsTree = new javax.swing.JTree();
    public final javax.swing.JSplitPane mainPanel = new javax.swing.JSplitPane();
    public final javax.swing.JTabbedPane mapPanel = new javax.swing.JTabbedPane();
    private javax.swing.JMenuBar menuBar;
    public final javax.swing.JButton openFileButton = new javax.swing.JButton();
    public final javax.swing.JMenuItem openFileMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JMenuItem openGraphMenuItem = new javax.swing.JMenuItem();
    private javax.swing.JMenuItem pasteMenuItem;
    public final javax.swing.JMenuItem printMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JMenuItem printPreviewFileMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JPanel propertiesPanel = new javax.swing.JPanel();
    public final javax.swing.JMenuItem saveAsFileMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JButton saveFileButton = new javax.swing.JButton();
    public final javax.swing.JMenuItem saveFileMenuItem = new javax.swing.JMenuItem();
    private javax.swing.JPanel searchPanel;
    public final javax.swing.JPopupMenu tabPopupMenu = new javax.swing.JPopupMenu();
    public final javax.swing.JToolBar toolBar = new javax.swing.JToolBar();
    public final javax.swing.JButton zoomInButton = new javax.swing.JButton();
    public final javax.swing.JButton zoomOutButton = new javax.swing.JButton();
    public final javax.swing.JButton zoomResetButton = new javax.swing.JButton();
    // End of variables declaration//GEN-END:variables

    // our modifications:
    static {
        // set nimbus theme
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
        }
    }

    public static EditorApplicationView viewInstance = new EditorApplicationView();

    public static synchronized EditorApplicationView instance()
    {
        return viewInstance;
    }

    public static synchronized void reset()
    {
        viewInstance = new EditorApplicationView();
    }
}
