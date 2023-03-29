package bot;

import api.ApiService;
import api.models.Changeset;
import api.models.Repository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Bot extends TelegramLongPollingBot {
    private static final String logger = "\n[ " + Bot.class.getName() + " ] ";
    private ApiService api;

    private List<Repository> oldRepos = new ArrayList<>();
    private HashMap<Long, User> users = new HashMap<>();
    private List<Changeset> changesets = new ArrayList<>();

    private final Buttons mainButtons = new Buttons(true);

    @Override
    public void onUpdateReceived(Update update) {
        Long chatId = update.getMessage().getChatId();
        String inputText = update.getMessage().getText();

        if (!users.keySet().contains(chatId)){
            users.put(chatId, new User(chatId));
        }
        User currentUser = users.get(chatId);
        Buttons userReposButtons = new Buttons(true);
        userReposButtons.setButtons(currentUser.getRepos());

        Buttons reposButtons = new Buttons(true);
        reposButtons.setButtons(api.getAllRepositories());

        switch (inputText) {
            case "/start":
                System.out.println(logger + " New user connect. ChatId: " + chatId);
                botSay(chatId, "Hello. This bot make for logging from Plastic SCM. \nPlease check repository what you need.", reposButtons);
                break;

            case "Back":
                currentUser.setDeleteAction(false);
                List<String> mainButtonNames = new ArrayList<>();
                mainButtonNames.add("Add repos");
                mainButtonNames.add("Delete repos");
                mainButtons.setButtons(mainButtonNames);
                botSay(chatId, "U can add new repos later.", mainButtons);
                break;

            case "Add repos":
                reposButtons.removeButtons(users.get(chatId).getRepos());
                botSay(chatId, "Choose new repos.", reposButtons);
                break;

            case "Delete repos":
                currentUser.setDeleteAction(true);
                botSay(chatId, "Choose repos what u want delete.", userReposButtons);
                break;

            default:
                if (currentUser.isDeleteAction()){
                    currentUser.deleteRepo(inputText);
                    userReposButtons.removeButton(inputText);
                    botSay(chatId, "Choose repos what u want delete.", userReposButtons);
                    break;
                }

                for (Repository repository : api.getAllRepositories()) {
                    String repoName = repository.getName();

                    if (inputText.equals(repoName)){
                        currentUser.addNewRepo(repoName);
                        reposButtons.removeButtons(currentUser.getRepos());
                        botSay(chatId, "U can choose one more repo.", reposButtons);
                        break;
                    }
                }
        }
    }

    private void botSay(long chatId, String data, Buttons returnButtons){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(data);
        try {
            if (returnButtons.isNeed()){
                setButtons(message, returnButtons.getButtons());
            }
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public synchronized void setButtons(SendMessage sendMessage, List<?> elements){
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        List<KeyboardRow> keyboard = new ArrayList<>();

        int i = 0;
        KeyboardRow keyboardFirstRow = new KeyboardRow();

        for (Object elem : elements) {
            if (i % 2 == 0 && i > 0){
                keyboard.add(keyboardFirstRow);
                keyboardFirstRow = new KeyboardRow();
            }
            String keyName;
            try {
                keyName = ((Repository) elem).getName();
            }catch (ClassCastException e){
                keyName = (String) elem;
            }

            keyboardFirstRow.add(keyName);
            i++;
        }
        keyboard.add(keyboardFirstRow);

        if (elements.stream().noneMatch(item -> item.toString().equals("Add repos"))) {
            KeyboardRow keyboardLastRow = new KeyboardRow();
            keyboardLastRow.add("Back");
            keyboard.add(keyboardLastRow);
        }

        replyKeyboardMarkup.setKeyboard(keyboard);
    }

    public void startApi(){
        api = getApiInstance();
        Thread thread = new Thread("New Thread") {
            public void run(){
            while (true) {
                checkRepos();
                printRepositoriesStats();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        };
        thread.start();
    }

    private void checkRepos(){
        if (oldRepos.size() != api.getAllRepositories().size()){
            System.out.println(logger + " Add new repo.\n");
            for (User user: users.values()) {
                botSay(user.getChatId(), "Added new repo, u can change your list of repos.", new Buttons(false));
            }

            oldRepos = api.getAllRepositories();
        }
    }

    private void printRepositoriesStats() {
        List<Changeset> differences = new ArrayList<>();
        List<Changeset> changesetsTemp = new ArrayList<>();
        for (Repository repo : oldRepos) {
            changesetsTemp.addAll(api.getAllChangesets(repo.getName(), getDate()));
            for (Changeset cha: changesetsTemp) {
                differences.add(cha);
                for (Changeset cha1 : changesets) {
                    if (cha1.toString().equals(cha.toString())) {
                        differences.remove(cha);
                        break;
                    }
                }
            }
        }

        if (differences.size() > 0){
            changesets = changesetsTemp;
            for (Changeset changeset: differences) {
                System.out.println(logger + " Plastic have new change.\n" + changeset.toString());
                for (User user: users.values()) {
                    if (user.getRepos().contains(changeset.getRepository().getName())){
                        botSay(user.getChatId(), changeset.toString(), new Buttons(false));
                        //botSay(changeset.toString());
                    }
                }
            }
        }
    }

    public ApiService getApiInstance() {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();

        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint("http://localhost:9090")
                .setConverter(new GsonConverter(gson))
                .build();

        return adapter.create(ApiService.class);
    }


    @Override
    public String getBotUsername() {
        return "";
    }

    @Override
    public String getBotToken() {
        return "";
    }

    @Override
    public void onClosing() {
        super.onClosing();
    }

    private String getDate(){
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
       // return "date%20>%20%27" + LocalDate.now().minusDays(1).format(sdf) + "%27";
        return "date > '" + LocalDate.now().minusDays(1).format(sdf) + "'";
    }

}
