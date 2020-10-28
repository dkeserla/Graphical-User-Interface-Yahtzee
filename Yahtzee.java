package com.dinesh.keserla;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;

public class Yahtzee extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        Button save = new Button("Save Score");
        YahtzeeCalculations game = new YahtzeeCalculations();
        DiceBox diceBox = new DiceBox(game,setImages(), getImage("0.png"));
        RollControls rollControls = new RollControls(game, diceBox);
        ScoreBox scoreBox = new ScoreBox(game,diceBox,rollControls,save);
        HighScores high = new HighScores(parseHighScores());

        TextInputDialog dialog = new TextInputDialog("Dinesh");
        dialog.setTitle("Welcome to Dinesh's Yahtzee");
        dialog.setHeaderText("We need to know who you are");
        dialog.setContentText("Please Enter Your Name:");
        Optional<String> result = dialog.showAndWait();
        String player = result.isPresent()? result.get() : "Unknown";
        Label name = new Label("Now Playing: " + player);


        save.setVisible(false);
        save.setOnAction(e -> {
            addToScoreBoard(game, player);
            high.setHighScores(parseHighScores());
            high.reset();
        });

        HBox nameScore = new HBox();
        nameScore.setAlignment(Pos.TOP_CENTER);
        nameScore.setSpacing(25);
        nameScore.getChildren().addAll(name, save);

        ImageView logo = new ImageView(getImage("heading.jpg"));
        logo.setFitHeight(195);
        logo.setFitWidth(400);

        VBox layout = new VBox();
        layout.setSpacing(40);
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        layout.getChildren().addAll(logo, nameScore, diceBox, rollControls, scoreBox, high);
        primaryStage.setTitle("YAHTZEE");
        primaryStage.setScene(new Scene(layout, 700, 950));
        primaryStage.show();


    }

    public Image getImage(String name) throws IOException {
        try(FileInputStream file = new FileInputStream("C:\\Code\\Yahtzee\\src\\com\\dinesh\\keserla\\static-images\\" + name)){
           return new Image(file);
        }
        catch (Exception e){
            throw e;
        }
    }
    public Image[] setImages() throws Exception {
        Image[] images = new Image[6];
        for(int i = 0; i < images.length; i++) {
            images[i] = getImage((i+1) + ".png");
        }
        return images;
    }
    public TreeMap<Integer, String> parseHighScores() {
        TreeMap<Integer,String> parse = new TreeMap<>(Collections.reverseOrder());
        try(BufferedReader bReader = new BufferedReader(new FileReader("C:\\Code\\Yahtzee\\src\\com\\dinesh\\keserla\\scores.dat"))){
            String line;
            while((line=bReader.readLine()) != null){
                Scanner l = new Scanner(line);
                parse.put(l.nextInt(),l.nextLine());
            }
        }
        catch (Exception e) {
            System.out.println(e);
        }
        return parse;
    }
    public void addToScoreBoard(YahtzeeCalculations game, String playerName){
        try(PrintWriter writer = new PrintWriter(new FileWriter("C:\\Code\\Yahtzee\\src\\com\\dinesh\\keserla\\scores.dat", true))) {
            writer.println(game.getGrandTotal() + " " + playerName);
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }


    class DiceBox extends HBox{
        private YahtzeeCalculations game;
        private Image[] images;
        private Image reset;

        DiceBox(YahtzeeCalculations game, Image[] images, Image reset) {
            this.setAlignment(Pos.TOP_CENTER);
            this.images = images;
            this.game = game;
            this.setSpacing(25);
            this.reset = reset;
            for(int i = 0; i < 5; i++){
                VBox x = new VBox();
                x.getChildren().addAll(getDiceImage(i+1),new Label("" + (i+1)),new Label("Dice " + (i+1)),new CheckBox("Keep"));
                x.setSpacing(10);
                x.setAlignment(Pos.BASELINE_CENTER);
                this.getChildren().add(x);
            }
            reset();
        }

        public ImageView getDiceImage(int x){
            ImageView dice = new ImageView(images[x-1]);
            dice.setPreserveRatio(true);
            dice.setFitHeight(50);
            dice.setFitWidth(50);
            return dice;
        }

        public ArrayList<Node> getAtEachIndex(int i){
            ArrayList<Node> nodes = new ArrayList<>();
            for(Node x : this.getChildren()) {
                nodes.add(((VBox) x).getChildren().get(i));
            }
            return nodes;
        }

        public void updateDice(int[] diceNums){
            for(int i = 0; i < diceNums.length; i++) {
                List<Node> segments = ((VBox)this.getChildren().get(i)).getChildren();
                segments.set(1, new Label(""+diceNums[i]));
                segments.set(0, getDiceImage(diceNums[i]));
            }
        }

        public ArrayList<Integer> getKeepIndexes(){
            List<Node> checkBoxes = getAtEachIndex(3);
            ArrayList<Integer> keepIndexes = new ArrayList<>();
            for(int i = 0; i < checkBoxes.size(); i++){
                if(!((CheckBox)(checkBoxes.get(i))).isSelected()) {
                    keepIndexes.add(i);
                }
            }
            return keepIndexes;
        }

        public void reset(){
            for(int i = 0; i < 5; i++) {
                ImageView reset = new ImageView(this.reset);
                reset.setFitWidth(50);
                reset.setFitHeight(50);
                List<Node> segments = ((VBox)this.getChildren().get(i)).getChildren();
                segments.set(1, new Label("0"));
                segments.set(0, reset);
                ((CheckBox)segments.get(3)).setSelected(false);
            }
        }
    }

    class DataLabel extends HBox{
        DataLabel(String name, String value){
            this.getChildren().addAll(new Label(name + ":   "), new Label(value));
        }

        DataLabel(String name, int value){
            this(name,""+value);
        }

        public void updateValue(String value){
            this.getChildren().set(1, new Label(value));
        }

        public void updateValue(int value){
            updateValue(""+value);
        }
    }

    class RollControls extends GridPane{
        private YahtzeeCalculations game;
        private DataLabel rollsLeft;
        private DataLabel turns;

        public RollControls(YahtzeeCalculations game, DiceBox diceBox){
            this.game = game;
            this.setAlignment(Pos.CENTER);
            this.setHgap(10);
            this.setVgap(10);

            Button b = new Button("Roll");
            b.setOnAction(e -> {
                if(game.canRoll()) {
                    ArrayList<Integer> indexes = diceBox.getKeepIndexes();
                    if(indexes.isEmpty()) {
                        game.roll();
                    }
                    else {
                        game.roll(indexes);
                    }
                    diceBox.updateDice(game.getRoll());
                    game.setRolls(game.getRolls()+1);
                    reset();
                }
            });

            rollsLeft = new DataLabel("Rolls Left", 3-game.getRolls());
            turns = new DataLabel("Turns", game.getTurn());

            this.add(b,0,0);
            this.add(rollsLeft,0,1);
            this.add(turns,1,1);
        }

        public void reset(){
            rollsLeft.updateValue((3-game.getRolls()));
            turns.updateValue(game.getTurn());
        }
    }

    class ScoreButton extends VBox{
        private YahtzeeCalculations game;
        private DiceBox diceBox;
        private RollControls rollControls;
        private ScoreBox scoreBox;

        ScoreButton(YahtzeeCalculations game, DiceBox diceBox, RollControls rollControls, ScoreBox scoreBox, int index, String name, Callable<Integer> action, Button save){
            this.game = game;
            this.diceBox = diceBox;
            this.rollControls = rollControls;
            this.scoreBox = scoreBox;
            Button score = new Button(name);
            score.setPrefWidth(100);
            score.setPrefHeight(5);
            score.setOnAction(e -> {
                if(game.getRolls() != 0 && game.updateScoreCard(index,calculate(action))){
                    game.updateTotals();
                    this.getChildren().set(0, new Label(""+game.getScoreCard()[index]));
                    game.setTurn(game.getTurn()+1);
                    reset();
                    if(game.getTurn() == 13){
                        save.setVisible(true);
                    }
                }
            });
            this.setAlignment(Pos.TOP_CENTER);
            this.getChildren().addAll(new Label(""), score);
        }

        public int calculate(Callable<Integer> action){
            try{
                return action.call();
            }
            catch(Exception x){
                System.out.println("BUTTON AINT WORKING");
            }
            return 0;
        }

        public void reset(){
            game.setRolls(0);
            diceBox.reset();
            rollControls.reset();
            scoreBox.reset();
        }
    }

    class ScoreBox extends GridPane{
        private YahtzeeCalculations game;
        private DataLabel topSum;
        private DataLabel bonus;
        private DataLabel total;

        ScoreBox(YahtzeeCalculations game, DiceBox diceBox, RollControls rollControls, Button save){
            this.game = game;
            this.setHgap(10);
            this.setVgap(10);
            this.setAlignment(Pos.TOP_CENTER);

            topSum = new DataLabel("Top Sum", 0);
            bonus = new DataLabel("Bonus", 0);
            total = new DataLabel("Total", 0);


            String[] names = new String[]{"Ones", "Twos", "Threes", "Fours", "Fives", "Sixes"};

            for(int i = 0; i < 6; i++){
                int finalI = i;
                ScoreButton button = new ScoreButton(game, diceBox, rollControls, this, i ,names[i], () -> game.calculateUpper(finalI + 1), save);
                this.add(button, i % 3, i / 3);
            }

            VBox top = new VBox();
            top.getChildren().addAll(new Label(),topSum);
            top.setSpacing(10);

            VBox bon = new VBox();
            bon.getChildren().addAll(new Label(),bonus);
            bon.setSpacing(10);

            this.add(top, 0,2);
            this.add(bon, 1,2);


            names = new String[]{"3 Kind", "4 Kind", "Full House", "Sm Str", "Lg Str", "Yahtzee", "Chance"};
            ArrayList<Callable<Integer>> functions = new ArrayList<>();
            functions.add(() -> game.calculate3Kind());
            functions.add(() -> game.calculate4Kind());
            functions.add(() -> game.calculateFullHouse());
            functions.add(() -> game.calculateSmallStraight());
            functions.add(() -> game.calculateLargeStraight());
            functions.add(() -> game.calculateYahtzee());
            functions.add(() -> game.calculateChance());

            for(int i = 0; i < names.length; i++){
                ScoreButton button = new ScoreButton(game, diceBox, rollControls,this,i + 6 ,names[i], functions.get(i), save);
                this.add(button, i % 3 + 3, i / 3);
            }


            VBox tot = new VBox();
            tot.getChildren().addAll(new Label(), total);
            tot.setSpacing(10);

            this.add(tot, 4,2);
        }
        public void reset(){
            topSum.updateValue(game.topSum());
            bonus.updateValue(game.getBonus());
            total.updateValue(game.getGrandTotal());
        }
    }

    class HighScores extends VBox{
        private TreeMap<Integer,String> highScores;
        HighScores(TreeMap<Integer,String> highScores){
            this.highScores = highScores;
            this.setAlignment(Pos.TOP_CENTER);
            this.setSpacing(10);
            reset();
        }


        public void setHighScores(java.util.TreeMap<Integer, String> highScores) {
            this.highScores = highScores;
        }

        public void reset(){
            List<Node> children = this.getChildren();
            this.getChildren().remove(0,children.size());
            this.getChildren().add(new Label("HIGHSCORES"));
            Set<Integer> highScoreValues = highScores.keySet();
            Iterator<Integer> high = highScoreValues.iterator();
            for(int i = 0; high.hasNext() && i < 5; i++) {
                Integer x = high.next();
                this.getChildren().add(new Label(highScores.get(x) + ":    " + x));
            }
        }
    }

}
