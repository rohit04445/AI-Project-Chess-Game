package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import engine.classic.board.Move;

class GameHistoryPanel extends JPanel {

    private final DataModel model;
    private static final Dimension HISTORY_PANEL_DIMENSION = new Dimension(100, 40);

    public GameHistoryPanel() {
        this.setLayout(new BorderLayout());
        this.model = new DataModel();
        final JTable table = new JTable(model);
        table.setRowHeight(15);
        final JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setColumnHeaderView(table.getTableHeader());
        scrollPane.setPreferredSize(HISTORY_PANEL_DIMENSION);
        this.add(scrollPane, BorderLayout.CENTER);
        this.setVisible(true);
    }

    public void redo(final List<Move> moveHistory) {
        int currentRow = 0;
        model.clear();
        for (final Move move : moveHistory) {
            if (move.getMovedPiece().getPieceAlliance().isWhite()) {
                this.model.setValueAt(move, currentRow, 0);
            }
            else if (move.getMovedPiece().getPieceAlliance().isBlack()) {
                this.model.setValueAt(move, currentRow, 1);
                currentRow++;
            }
        }

    }

    private static class Row {

        private Move whiteMove;
        private Move blackMove;

        public Row() {
        }

        public Move getWhiteMove() {
            return this.whiteMove;
        }

        public Move getBlackMove() {
            return this.blackMove;
        }

        public void setWhiteMove(final Move move) {
            this.whiteMove = move;
        }

        public void setBlackMove(final Move move) {
            this.blackMove = move;
        }

    }

    private static class DataModel extends DefaultTableModel {

        private final List<Row> values;
        private static final String[] NAMES = {"White", "Black"};

        public DataModel() {
            values = new ArrayList<>();
        }

        public void clear() {
            values.clear();
            setRowCount(0);
        }

        @Override
        public int getRowCount() {
            if(values == null) {
                return 0;
            }
            return values.size();
        }

        @Override
        public int getColumnCount() {
            return NAMES.length;
        }

        @Override
        public Object getValueAt(final int row, final int col) {
            final Row currentRow = values.get(row);
            if(col == 0) {
                return currentRow.getWhiteMove();
            } else if (col == 1) {
                return currentRow.getBlackMove();
            }
            return null;
        }

        @Override
        public void setValueAt(final Object aValue,
                               final int row,
                               final int col) {
            final Row currentRow;
            if(values.size() <= row) {
                currentRow = new Row();
                values.add(currentRow);
            } else {
                currentRow = values.get(row);
            }
            if(col == 0) {
                currentRow.setWhiteMove((Move) aValue);
                fireTableRowsInserted(row, row);
            } else  if(col == 1) {
                currentRow.setBlackMove((Move)aValue);
                fireTableCellUpdated(row, col);
            }
        }

        @Override
        public Class<?> getColumnClass(final int col) {
            return Move.class;
        }

        @Override
        public String getColumnName(final int col) {
            return NAMES[col];
        }
    }
}