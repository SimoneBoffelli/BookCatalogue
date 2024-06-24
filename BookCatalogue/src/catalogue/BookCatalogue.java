package catalogue;

import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author simob
 */
public class BookCatalogue extends javax.swing.JFrame {

    //costanti interne per definire le colonne della jTable
    private static final int ISBN_COLUMN_ID = 0;
    private static final int TITLE_COLUMN_ID = 1;
    private static final int AUTHOR_COLUMN_ID = 2;
    private static final int YEAR_COLUMN_ID = 3;

    //costanti interne per definire le opzioni di riordinamento
    private static final int ISBN_DOWN_ID = 0;
    private static final int ISBN_UP_ID = 1;
    private static final int TITLE_DOWN_ID = 2;
    private static final int TITLE_UP_ID = 3;
    private static final int AUTHOR_DOWN_ID = 4;
    private static final int AUTHOR_UP_ID = 5;
    private static final int YEAR_DOWN_ID = 6;
    private static final int YEAR_UP_ID = 7;

    //variabile per gestire la connessione al db
    private Connection conn;

    // costruttore
    public BookCatalogue() {

        initComponents(); //ciamata al metodo per inizializzare i componenti della gui

        this.openDB(); //chiamata al metodo per aprire la connessione con il db

        // Creazione e configurazione del modello di selezione per una tabella (JTable).
        ListSelectionModel selectionModel = this.bookTable.getSelectionModel();
        selectionModel.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {

                // Se l'evento di selezione è in corso di modifica, non fare nulla.
                if (e.getValueIsAdjusting()) {
                    return;
                }

                // Ottiene l'indice della riga selezionata.
                int selRow = bookTable.getSelectedRow();

                // Se nessuna riga è selezionata, non fare nulla.
                if (selRow < 0) {
                    return;
                }

                // Ottiene il modello della tabella e lo casta a DefaultTableModel.
                DefaultTableModel dtm = (DefaultTableModel) bookTable.getModel();

                // Imposta i valori dei campi di testo in base alla riga selezionata nella tabella.
                isbnTextField.setText((String) dtm.getValueAt(selRow,
                        ISBN_COLUMN_ID));
                titleTextField.setText((String) dtm.getValueAt(selRow,
                        TITLE_COLUMN_ID));
                authorTextField.setText((String) dtm.getValueAt(selRow,
                        AUTHOR_COLUMN_ID));
                yearsTextField.setText((String) dtm.getValueAt(selRow,
                        YEAR_COLUMN_ID));

                // Chiamata a un metodo per aggiornare l'interfaccia grafica.
                updateGUI();
            }
            // Chiusura della definizione del ListSelectionListener.
        });

        // Chiamata a un metodo per popolare la tabella con dati 
        this.populateTable();

    }

    //--------------------- metodi ---------------------------------------------
    private void updateGUI() {
        //ottiene l'indice della riga selezionata della tabella
        int selRow = this.bookTable.getSelectedRow();

        //ottiene il conteggio tatale delle righe della tabella
        int rowCount = this.bookTable.getRowCount();

        //controlla se i campi di testo non sono vuoti
        boolean isValidEntity = !this.isbnTextField.getText().isBlank()
                && !this.titleTextField.getText().isBlank()
                && !this.authorTextField.getText().isBlank()
                && !this.yearsTextField.getText().isBlank();
        
        boolean isValidUpdate = !this.searchTextField.getText().isBlank();

        //abilita il pulsante add solo se tutti i campi di testo sono validi
        this.insertButton.setEnabled(isValidEntity);

        //abilita il pulsante delete solo se c'e' una righa selezionata nella tabella
        this.deleteButton.setEnabled(selRow >= 0);

        //abilita il pulsante update solo se ci sono righe nella tabella
        this.updateButton.setEnabled(isValidUpdate);
        this.selectButton.setEnabled(isValidUpdate);

    }

    private void openDB() {
        try {
            // Carica la classe del driver JDBC per SQLite.
            Class.forName("org.sqlite.JDBC");

            // Stabilisce una connessione con il database SQLite specificato.
            this.conn = DriverManager.getConnection("jdbc:sqlite:BookCatalogue.db");

            // Crea un oggetto Statement per eseguire query SQL.
            try (Statement stmt = this.conn.createStatement()) {
                // Definisce una stringa SQL per creare una tabella se non esiste già.
                String sql = "CREATE TABLE IF NOT EXISTS bookCatalogue ("
                        + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + " isbn TEXT NOT NULL UNIQUE,"
                        + " title TEXT NOT NULL,"
                        + " author TEXT NOT NULL,"
                        + " years TEXT NOT NULL)";
                // Esegue l'aggiornamento SQL per creare la tabella.
                stmt.executeUpdate(sql);
            }
        } catch (ClassNotFoundException | SQLException ex) {
            // Cattura eccezioni relative alla connessione al database o al caricamento del driver.

            // Ottiene il messaggio di errore e lo modifica se necessario.
            String err = ex.getMessage().trim();
            if (ex instanceof ClassNotFoundException) {
                err = "Class '" + err + "' not found.";
            }

            // Mostra un messaggio di errore all'utente.
            JOptionPane.showMessageDialog(null,
                    err + "\n\nTerminate the application!",
                    "Prova test 2 Error",
                    JOptionPane.ERROR_MESSAGE);

            // Termina l'applicazione in caso di errore.
            this.dispatchEvent(new WindowEvent(this,
                    WindowEvent.WINDOW_CLOSING));
        }
    }

    private void populateTable() {
        //inizializza la stringa SQL per la selezione di tutti i dati della tabella
        String sql = "SELECT * FROM bookCatalogue ORDER BY ";

        //ottiene l'indice dell'elemento selezionato nel combobox per determinare ordinamento
        int selItem = this.sortComboBox.getSelectedIndex();

        //switch per modificare la stringa SQL in base all'elemento selezionato
        switch (selItem) {
            case ISBN_DOWN_ID:
                sql += "isbn ASC";
                break;
            case ISBN_UP_ID:
                sql += "isbn DESC";
                break;
            case TITLE_DOWN_ID:
                sql += "title ASC";
                break;
            case TITLE_UP_ID:
                sql += "title DESC";
                break;
            case AUTHOR_DOWN_ID:
                sql += "author ASC";
                break;
            case AUTHOR_UP_ID:
                sql += "author DESC";
                break;
            case YEAR_DOWN_ID:
                sql += "years ASC";
                break;
            case YEAR_UP_ID:
                sql += "years DESC";
                break;
            default:
                sql += "title ASC";
        }

        //ottiene il modello della tabella e resetta il numero di tighe a zero
        DefaultTableModel dtm = (DefaultTableModel) this.bookTable.getModel();
        dtm.setRowCount(0);

        //prepara la quarry SQL e la esegue
        try (PreparedStatement pstmt = this.conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                // Itera sui risultati della query.
                while (rs.next()) {
                    // Crea un array con i dati di una riga (titolo, descrizione, scadenza).
                    String[] rowData = {
                        rs.getString("isbn"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("years")
                    };
                    // Aggiunge la riga di dati al modello della tabella.
                    dtm.addRow(rowData);
                }
            }
        } catch (SQLException ex) {
            // Mostra un messaggio di errore in caso di eccezione SQL.
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "book catalogue Manager Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        // Aggiorna i componenti dell'interfaccia utente.
        this.updateGUI();
    }

    private void insert(String campo1, String campo2, String campo3, String campo4) {

        // Verifica che nessuno dei campi di testo sia vuoto.
        if (campo1.isBlank() || campo2.isBlank() || campo3.isBlank() || campo4.isBlank()) {
            return;
        }

        // Prepara una query SQL per inserire i nuovi dati nella tabella
        String sql = "INSERT INTO bookCatalogue (isbn, title, author, years) VALUES(?, ?, ?, ?)";
        try (PreparedStatement pstmt = this.conn.prepareStatement(sql)) {

            // Imposta i valori dei parametri nella query SQL.
            pstmt.setString(1, campo1);
            pstmt.setString(2, campo2);
            pstmt.setString(3, campo3);
            pstmt.setString(4, campo4);

            // Esegue l'aggiornamento del database
            pstmt.executeUpdate();

            // Aggiorna i dati visualizzati nella tabella.
            this.populateTable();
            
            this.emptyTextField();
            
            this.confermLabel.setText("Record aggiunto");

        } catch (SQLException ex) {
            // Mostra un messaggio di errore in caso di eccezione SQL.
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Book Catalogue Manager Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void delete(String entityName) {

        // Prepara la query SQL per eliminare il record.
        String sql = "DELETE FROM bookCatalogue WHERE isbn = ?";

        try (PreparedStatement pstmt = this.conn.prepareStatement(sql)) {

            // Imposta il parametro della query (campo del listener) e esegue l'aggiornamento.
            pstmt.setString(1, entityName);
            pstmt.executeUpdate();

            // Aggiorna i dati visualizzati nella tabella.
            this.populateTable();
            
            this.confermLabel.setText("Record cancellato");
            
            this.emptyTextField();
            
            this.updateGUI();

        } catch (SQLException ex) {

            // Mostra un messaggio di errore in caso di eccezione SQL.
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "book catalogue Manager Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void selec(String entityName) {
        // Definisce un metodo per selezionare un utente dal database usando la sua email.

        String sql = "SELECT * FROM bookCatalogue WHERE isbn = ?";

        // Prepara una query SQL per selezionare tutti i campi dalla tabella
        try (PreparedStatement pstmt = this.conn.prepareStatement(sql)) {
            // Crea un PreparedStatement all'interno di un blocco try-with-resources per gestire automaticamente la chiusura della risorsa.

            pstmt.setString(1, entityName);
            // Imposta il parametro email nella query SQL al posto del segnaposto '?'.

            try (ResultSet rs = pstmt.executeQuery()) {
                // Esegue la query e utilizza un altro try-with-resources per gestire il ResultSet 'rs'.

                if (rs.next()) {
                    // Se il ResultSet contiene almeno una riga, procede a leggere i dati.

                    // Imposta i valori dei campi di testo con i dati recuperati dal ResultSet.
                    this.isbnTextField.setText(rs.getString("isbn"));
                    this.titleTextField.setText(rs.getString("title"));
                    this.authorTextField.setText(rs.getString("author"));
                    this.yearsTextField.setText(rs.getString("years"));
                }
                // Recupera i valori dei campi selezionato e li assegna ai rispettivi campi di testo nell'interfaccia utente.
            }
        } catch (SQLException ex) {
            // Gestisce eventuali eccezioni SQL che possono verificarsi durante l'esecuzione della query o la connessione al database.

            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Book catalogue Manager Error",
                    JOptionPane.ERROR_MESSAGE);
            // Mostra un messaggio di errore in una finestra di dialogo se si verifica un'eccezione.
        }
    }

    private void update(String campo1, String campo2, String campo3, String campo4) {
        // Definisce un metodo per aggiornare i dati di un utente nel database. 

        if (campo1.isBlank() || campo2.isBlank() || campo3.isBlank() || campo4.isBlank()) {
            // Controlla se uno qualsiasi dei campi è vuoto o contiene solo spazi bianchi. 
            // Se è così, termina l'esecuzione del metodo senza fare nulla.

            return;
        }

        String sql = "UPDATE bookCatalogue SET isbn = ?, title = ?, author = ?, years = ? WHERE isbn = ?";

        // Prepara una query SQL per aggiornare i campi
        // dove la colonna 'WHERE' corrisponde al valore fornito.
        try (PreparedStatement pstmt = this.conn.prepareStatement(sql)) {
            // Crea un PreparedStatement all'interno di un blocco try-with-resources per gestire automaticamente la chiusura della risorsa.
            // 'this.conn' si riferisce a una connessione al database esistente.

            pstmt.setString(1, campo1);
            pstmt.setString(2, campo2);
            pstmt.setString(3, campo3);
            pstmt.setString(4, campo4);
            // Imposta i parametri nella query SQL ai valori forniti.

            int updatedRows = pstmt.executeUpdate();
            // Esegue l'aggiornamento e restituisce il numero di righe modificate nel database.

            if (updatedRows > 0) {
                // Se almeno una riga è stata aggiornata
                this.populateTable();
                // aggiornare o riempire una tabella nell'interfaccia utente con i dati aggiornati.
                
                this.confermLabel.setText("Record aggiornato correttamente");
            }
        } catch (SQLException ex) {
            // Gestisce eventuali eccezioni SQL che possono verificarsi durante l'esecuzione della query o la connessione al database.

            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "User Database Manager Error",
                    JOptionPane.ERROR_MESSAGE);
            // Mostra un messaggio di errore in una finestra di dialogo se si verifica un'eccezione.
        }
    }

    private void emptyTextField() {
        //svuota i campi di testo (da usare per esempio con il bottone reload)
        this.isbnTextField.setText("");
        this.titleTextField.setText("");
        this.authorTextField.setText("");
        this.yearsTextField.setText("");
        this.searchTextField.setText("");
    }

    //--------------------------------------------------------------------------
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollPane = new javax.swing.JScrollPane();
        bookTable = new javax.swing.JTable();
        isbnLabel = new javax.swing.JLabel();
        titleLabel = new javax.swing.JLabel();
        authorLabel = new javax.swing.JLabel();
        yearLabel = new javax.swing.JLabel();
        isbnTextField = new javax.swing.JTextField();
        titleTextField = new javax.swing.JTextField();
        authorTextField = new javax.swing.JTextField();
        yearsTextField = new javax.swing.JTextField();
        insertButton = new javax.swing.JButton();
        updateButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        searchLabel = new javax.swing.JLabel();
        sortLabel = new javax.swing.JLabel();
        connectionStatusLabel = new javax.swing.JLabel();
        searchTextField = new javax.swing.JTextField();
        sortComboBox = new javax.swing.JComboBox<>();
        connectButton = new javax.swing.JButton();
        refreshButton = new javax.swing.JButton();
        selectButton = new javax.swing.JButton();
        confermLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Book Catalogue");
        setMinimumSize(new java.awt.Dimension(950, 600));

        bookTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ISBN", "TITLE", "AUTHOR", "YEAR"
            }
        ));
        scrollPane.setViewportView(bookTable);

        isbnLabel.setText("ISBN:");

        titleLabel.setText("Title:");

        authorLabel.setText("Author:");

        yearLabel.setText("Year:");

        isbnTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                unlokButtons(evt);
            }
        });

        titleTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                unlokButtons(evt);
            }
        });

        authorTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                unlokButtons(evt);
            }
        });

        yearsTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                unlokButtons(evt);
            }
        });

        insertButton.setText("Insert");
        insertButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertButtonActionPerformed(evt);
            }
        });

        updateButton.setText("Update");
        updateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateButtonActionPerformed(evt);
            }
        });

        deleteButton.setText("Delete");
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        searchLabel.setText("Search ISBN:");

        sortLabel.setText("Sort column by:");

        searchTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                unlokButtons(evt);
            }
        });

        sortComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "ISBN DOWN", "ISBN UP", "TITLE DOWN", "TITLE UP", "AUTHOR DOWN", "AUTHOR UP", "YEARS DOWN", "YEARS UP" }));
        sortComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sortComboBoxActionPerformed(evt);
            }
        });

        connectButton.setText("Connect");
        connectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectButtonActionPerformed(evt);
            }
        });

        refreshButton.setText("Refresh");
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        selectButton.setText("Select");
        selectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(authorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(authorTextField))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(titleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(titleTextField))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(isbnLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(isbnTextField))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(yearLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(34, 34, 34)
                                        .addComponent(insertButton, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(12, 12, 12)
                                        .addComponent(updateButton, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(deleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 41, Short.MAX_VALUE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(yearsTextField)))))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(connectionStatusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(connectButton, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(searchLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(sortLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(sortComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(searchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 283, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(selectButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(189, 189, 189)
                                .addComponent(confermLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 249, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addComponent(scrollPane))
                .addGap(15, 15, 15))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                .addGap(29, 29, 29)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(isbnLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(isbnTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchLabel)
                    .addComponent(searchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectButton))
                .addGap(14, 14, 14)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(titleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(titleTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sortLabel)
                    .addComponent(sortComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(refreshButton))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(authorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(authorTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(15, 15, 15)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(yearLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(yearsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(confermLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)))
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(insertButton)
                    .addComponent(updateButton)
                    .addComponent(deleteButton)
                    .addComponent(connectionStatusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(connectButton))
                .addGap(30, 30, 30))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void insertButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertButtonActionPerformed
        // Ottiene i valori dai campi di testo della GUI. (da usare nel metodo)                                        
        String tl = this.isbnTextField.getText();
        String t2 = this.titleTextField.getText();
        String t3 = this.authorTextField.getText();
        String t4 = this.yearsTextField.getText();

        // chiamata al metodo
        insert(tl, t2, t3, t4);
    }//GEN-LAST:event_insertButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        // Mostra un dialogo di conferma per assicurarsi che l'utente voglia davvero eliminare il record.
        int response = JOptionPane.showConfirmDialog(this,
                "Do you really want to delete the selected record from the database?",
                "book catalogue Manager",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        // Interrompe l'azione se l'utente sceglie "No".
        if (response != JOptionPane.YES_OPTION) {
            return;
        }

        // Ottiene l'indice della riga selezionata nella tabella.
        int selectedRow = this.bookTable.getSelectedRow();

        // Interrompe l'azione se nessuna riga è selezionata.
        if (selectedRow < 0) {
            return;
        }

        // Ottiene il titolo della riga selezionata, che è usato come chiave per l'eliminazione.
        String entityName = (String) this.bookTable.getValueAt(selectedRow, ISBN_COLUMN_ID);

        // Chiama il metodo deleteTask per eliminare il record selezionato.
        delete(entityName);
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void selectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectButtonActionPerformed
        String entityName = searchTextField.getText();
        // Recupera il testo dall'elemento

        if (!entityName.isBlank()) {
            // Controlla se il campo email non è vuoto o non contiene solo spazi bianchi.

            selec(entityName);
            // Se l'email è valida, chiama il metodo 'select'

        } else {
            // Se il campo email è vuoto o contiene solo spazi bianchi, mostra un messaggio di errore.

            JOptionPane.showMessageDialog(null,
                    "Please enter a valid isbn to select a book.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            // Usa JOptionPane per mostrare una finestra di dialogo con un messaggio di errore, indicando all'utente di inserire un'email valida.
        }
    }//GEN-LAST:event_selectButtonActionPerformed

    private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateButtonActionPerformed
        String t1 = isbnTextField.getText();
        String t2 = titleTextField.getText(); //<------------ DA MODIFICARE
        String t3 = authorTextField.getText();
        String t4 = yearsTextField.getText();
        // Recupera i testi dai campi di testo dell'interfaccia grafica

        if (!t1.isBlank() && !t2.isBlank() && !t3.isBlank() && !t4.isBlank()) {
            // Controlla se nessuno dei campi è vuoto o contiene solo spazi bianchi.

            update(t1, t2, t3, t4);
            // Se tutti i campi sono validi, chiama il metodo 'update' con i valori forniti.

            //svuota i campi di testo
            this.emptyTextField(); //<------------ da mettere o nel listener o nel metodo o da togliere

            //conferma riuscita dell'update
            confermLabel.setText("Aggiornamento completato con successo");

        } else {
            // Se almeno uno dei campi è vuoto o contiene solo spazi bianchi, mostra un messaggio di errore.
            JOptionPane.showMessageDialog(null,
                    "All fields must be filled to update a book.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            // Usa JOptionPane per mostrare una finestra di dialogo con un messaggio di errore, 
        }
    }//GEN-LAST:event_updateButtonActionPerformed

    private void connectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectButtonActionPerformed
        // Metodo per gestire la connessione al database, alternando tra connessione e disconnessione.

        // Se la connessione è attualmente chiusa, tenta di aprirla.
        if (this.conn == null) {
            openDB();
            updateGUI();
            // Cambia il testo del bottone di connessione a "Disconnect", indicando che ora è possibile disconnettersi.
            connectButton.setText("Disconnect");
        } else {
            // Se la connessione è attualmente aperta, tenta di chiuderla.

            try {
                this.conn.close();
                // Chiude la connessione al database.

                this.conn = null;
                // Imposta l'oggetto connessione 'conn' a null, indicando che la connessione è stata chiusa.

                JOptionPane.showMessageDialog(this, "Database Disconnected",
                        "Connection Status", JOptionPane.INFORMATION_MESSAGE);
                // Mostra un messaggio di informazione che indica che la connessione al database è stata chiusa con successo.

                // Cambia il testo del bottone di connessione a "Connect", indicando che ora è possibile connettersi.
                connectButton.setText("Connect");
            } catch (SQLException ex) {
                // Gestisce eccezioni specifiche di SQL che possono verificarsi durante il tentativo di disconnessione dal database.

                JOptionPane.showMessageDialog(this, "Failed to disconnect from the database: " + ex.getMessage(),
                        "Disconnection Error", JOptionPane.ERROR_MESSAGE);
                // Mostra un messaggio di errore se la disconnessione dal database non riesce.
            }
            updateGUI();
        }
    }//GEN-LAST:event_connectButtonActionPerformed

    private void sortComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sortComboBoxActionPerformed
        // Questo metodo viene chiamato quando l'utente cambia la selezione nella ComboBox di ordinamento.

        // Ricarica e riordina i dati nella tabella in base alla nuova selezione.
        this.populateTable();
    }//GEN-LAST:event_sortComboBoxActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        // Questo metodo viene chiamato quando l'utente clicca sul pulsante di ricaricamento.

        // Ricarica i dati nella tabella.
        this.populateTable();
        
        this.emptyTextField();
        
        this.confermLabel.setText("");
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void unlokButtons(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_unlokButtons
        // Questo metodo viene chiamato quando l'utente rilascia un tasto mentre scrive in uno dei campi di testo.

        // Aggiorna l'interfaccia utente per riflettere i cambiamenti nei campi di testo.
        this.updateGUI();
    }//GEN-LAST:event_unlokButtons

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
            java.util.logging.Logger.getLogger(BookCatalogue.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(BookCatalogue.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(BookCatalogue.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(BookCatalogue.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new BookCatalogue().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel authorLabel;
    private javax.swing.JTextField authorTextField;
    private javax.swing.JTable bookTable;
    private javax.swing.JLabel confermLabel;
    private javax.swing.JButton connectButton;
    private javax.swing.JLabel connectionStatusLabel;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton insertButton;
    private javax.swing.JLabel isbnLabel;
    private javax.swing.JTextField isbnTextField;
    private javax.swing.JButton refreshButton;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JTextField searchTextField;
    private javax.swing.JButton selectButton;
    private javax.swing.JComboBox<String> sortComboBox;
    private javax.swing.JLabel sortLabel;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JTextField titleTextField;
    private javax.swing.JButton updateButton;
    private javax.swing.JLabel yearLabel;
    private javax.swing.JTextField yearsTextField;
    // End of variables declaration//GEN-END:variables
}
