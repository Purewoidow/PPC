package pingpongcryptor;

import java.net.URL;
import java.util.Map;
import javafx.fxml.FXML;
import java.util.HashMap;
import javafx.concurrent.Task;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.concurrent.Service;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.control.TextArea;
import static javafx.concurrent.Worker.State.RUNNING;

/**
 * FXML Controller class
 *
 * @author Aamir khan
 */
public class FXMLDocumentController implements Initializable {

    @FXML
    private TextArea input;
    @FXML
    private TextArea output;
    @FXML
    private Label counter;
    @FXML
    private Label progress;
    @FXML
    private Label progressMsg;
    @FXML
    private Button btn;
    @FXML
    private Label stopTask;

    final ImageView DONE_ICON = new ImageView(
            new Image(getClass().getResourceAsStream("sign-check.png")));

    StringBuilder demoText;

    private Map<Character, Character> encodingMap;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        init();
        prepareMap(encodingMap);
        bindNodes();
    }

    @FXML
    private void encrypt(ActionEvent event) {
        startTask();
    }//encrypt ENDS

    Service<Void> task = new Service() {

        @Override
        protected Task createTask() {
            return new Task<Void>() {

                long start = 0L;
                long stop = 0L;

                @Override
                protected Void call() throws Exception {
                    String inputText = input.getText();
                    final float INPUT_LEN = inputText.length();
                    StringBuilder result = new StringBuilder((int)INPUT_LEN);

                    start = System.nanoTime();

                    for (char c : inputText.toCharArray()) {
                        Character ch = encodingMap.getOrDefault(c, c);
                        result.append(ch);

                        String percent = String.format("%.2f", (result.length() / INPUT_LEN) * 100);
                        updateMessage("Processing... " + percent + "%");

                        if (isCancelled()) {
                            updateMessage("Task Was Cancelled " + percent + "% was done");
                            break;
                        }

                    }
                    stop = System.nanoTime();

                    Platform.runLater(() -> {
                        output.setText(result.toString());
                    });

                    return null;
                }

                @Override
                protected void succeeded() {
                    progressMsg.setText("Done. in " + (stop - start) / 1000000000.0 + " sec");
                    progressMsg.setGraphic(DONE_ICON);
                    updateMessage("");
                }

            };
        }

    };

    boolean wasStartedBefore = false;

    /**
     * start or restart the task
     */
    private void startTask() {

        if (wasStartedBefore) {
            task.restart();
        } else {
            task.start();
            wasStartedBefore = true;
        }

    }

    @FXML
    private void cancleTask() {
        progress.setTextFill(Color.RED);
        task.cancel();
    }

    /**
     * fills the @link{Map} with chars according to the algo <strong> ((i + 13) % 26 +c)</strong>
     * <br> where i represents [a-z]
     * <br> where c represents the range of [a-z] and [A-Z]
     * <br>
     * <b>Note:the algorithm was copied form the python Module called "this"</b>
     *
     * @param map
     */
    private void prepareMap(Map map) {
        //ExecutorService mappingTask = Executors.newFixedThreadPool(1);
        //mappingTask.submit(() -> {
        System.out.println("preparing map");
        for (int c : new int[]{65, 97}) {

            for (int i = 0; i < 26; i++) {

                Character key = (char) (i + c);
                Character value = (char) ((i + 13) % 26 + c);

                map.put(key, value);
            }
        }
        //        });
        //mappingTask.shutdown();
    }//prepareMap ENDS

    //avoid re generating the text
    boolean wasTextNotGeneratedBefore = true;

    /**
     * Fill input field with some demo text
     */
    @FXML
    private void fillWithDemoText() {
        //if Text was not generated before genetate it
        if (wasTextNotGeneratedBefore) {
            generateDemoTextAndFill();
            wasTextNotGeneratedBefore = false;
        } //if text was generated before fill the input with some random text > 200000 characteres
        else {
            //just fill with some random data
            input.setText(demoText.substring(0, (int) (200000 + Math.random() * 500)));
        }

    }//fillWithDemoText ends

    /**
     * <p>
     * Bind the properties to keep track the state of app  <br>
     * - keep track of input field changes and update the counter <br>
     * - keep track of input field if its empty disable the encrypt/decrypt
     * button <br>
     * - keep track of task property if a task is running disable the
     * encrypt/decrypt button till task is running<br>
     * - keep update the progress Msg while task is running<br>
     * - show the stop task label as the user start the task - Mor to come
     * </p>
     *
     */
    private void bindNodes() {

        //keep track of input field changes and update the counter
        counter.textProperty().bind(input.textProperty().length().asString());

        //keep track of input field if its empty disable the encrypt/decrypt button
        btn.disableProperty().bind(input.textProperty().isEmpty()
                //keep track of task property if a task is running disable the encrypt/decrypt button till task is running        
                .or(task.stateProperty().isEqualTo(RUNNING))
        );
        //keep update the progress Msg while task is running
        progress.textProperty().bind(task.messageProperty());
        //show the stop task label as the user start the task
        stopTask.visibleProperty().bind(task.stateProperty().isEqualTo(RUNNING));

    }

    /**
     * initialize things
     */
    private void init() {
        encodingMap = new HashMap<>(52);
        stopTask.setTooltip(new Tooltip("Cancle Task"));
        //Some House keeping 
        task.setOnReady(e -> {
            progressMsg.setText("");
            progressMsg.setGraphic(null);
            progress.setTextFill(Color.web("#078ffa"));
            output.clear();
        });
    }

    /**
     * Generate some Demo Text and fill the input filed with the Text
     */
    private void generateDemoTextAndFill() {

        new Thread() {//in future i am gonna use Executors

            String data = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod\n"
                    + "tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam,\n"
                    + "quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo\n"
                    + "consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse\n"
                    + "cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non\n"
                    + "proident, sunt in culpa qui officia deserunt mollit anim id est laborum.\n\n";

            @Override
            public void run() {
                final int BUILDER_SIZE = (data.length() * data.length());
                demoText = new StringBuilder(BUILDER_SIZE);

                for (int i = 0; i < data.length(); i++) {
                    demoText.append(data);
                }

                Platform.runLater(() -> {
                    input.setText(demoText.toString());
                });

            }

        }.start();
    }

}
