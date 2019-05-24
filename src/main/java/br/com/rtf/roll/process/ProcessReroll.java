package br.com.rtf.roll.process;

import java.util.List;
import java.util.Random;

public class ProcessReroll implements Process {
  private String result;
  private long total;
  private String reroll;
  private List<Integer> rolls;
  private int sides;

  public ProcessReroll(String result, long total, String reroll, List<Integer> rolls, int sides) {
    this.result = result;
    this.total = total;
    this.reroll = reroll;
    this.rolls = rolls;
    this.sides = sides;
  }

  @Override
  public String getResult() {
    return result;
  }

  @Override
  public long getTotal() {
    return total;
  }

  @Override
  public Process invoke() {
    Random random = new Random();

    if (reroll == null) {
      reroll = "1";
    }
    int reroll = Integer.parseInt(this.reroll);
    result = result.concat(" (");
    for (Integer roll : rolls) {
      if (roll > reroll) {
        total = total + roll;
        result = result.concat(" **").concat(String.valueOf(roll).concat("**"));
      } else {
        int newRoll = random.nextInt(sides) + 1;
        total = total + newRoll;
        result = result.concat(" __").concat(String.valueOf(roll)).concat("__");
        result = result.concat(" **").concat(String.valueOf(newRoll).concat("**"));
      }
    }
    result = result.concat(" )");
    return this;
  }
}