package uk.co.mpcontracting.rpmjukebox.component;

import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldListCell;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;

public class PlaylistListCell extends TextFieldListCell<Playlist> {

    public PlaylistListCell(PlaylistStringConverter<Playlist> stringConverter) {
        super(stringConverter);
    }

    @Override
    public void updateItem(Playlist playlist, boolean empty) {
        super.updateItem(playlist, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            ((PlaylistStringConverter<Playlist>) getConverter()).setPlaylist(playlist);

            if (playlist.getPlaylistId() < 0) {
                setEditable(false);
            }

            // If we're updating the cell and the graphic is a TextField, then
            // we're being put into auto-edit mode, so select all the text in
            // the TextField and set the text in the Label to null
            if (getGraphic() instanceof TextField) {
                ((TextField) getGraphic()).selectAll();

                setText(null);
            } else {
                setText(playlist.getName());
            }
        }
    }

    @Override
    public void startEdit() {
        super.startEdit();

        // If the TextField loses focus, commit the edit
        if (getGraphic() instanceof TextField) {
            TextField textField = (TextField) getGraphic();

            textField.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
                if (newValue != null && !newValue) {
                    commitEdit((getConverter()).fromString(textField.getText()));
                }
            });
        }
    }
}
