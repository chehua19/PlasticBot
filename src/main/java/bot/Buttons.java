package bot;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Buttons {
    private boolean isNeed;
    private List<? extends Object> buttons;

    public Buttons(boolean isNeed){
        this.isNeed = isNeed;
        this.buttons = new ArrayList<>();
    }

    public boolean isNeed() {
        return isNeed;
    }

    public List<?> getButtons() {
        return buttons;
    }

    public void setButtons(List<?> buttons) {
        this.buttons = buttons;
    }

    public void removeButtons(List<?> buttonsToRemove){
        buttons.removeAll(buttons.stream().filter(button -> {
            try {
                for (Object obj : buttonsToRemove) {
                    if (obj.toString().equals(button.toString())) return true;
                }
            }catch (NullPointerException ignored) { }
            return false;
        }).collect(Collectors.toCollection(ArrayList::new)));
    }

    public void removeButton(Object buttonToRemove){
        buttons.remove(buttonToRemove.toString());
    }
}
