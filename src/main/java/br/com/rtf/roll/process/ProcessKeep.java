package br.com.rtf.roll.process;

import java.util.List;

public class ProcessKeep implements Process {
  private String result;
  private long total;
  private String keep;
  private List<Integer> rolls;

  public ProcessKeep(String result, long total, String keep, List<Integer> rolls) {
    this.result = result;
    this.total = total;
    this.keep = keep;
    this.rolls = rolls;
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
    if (keep == null) {
      keep = "1";
    }
    int keep = Integer.parseInt(this.keep);
    result = result.concat(" (");
    for (Integer roll : rolls) {
      if (keep >= 1) {
        total = total + roll;
        result = result.concat(" **").concat(String.valueOf(roll).concat("**"));
      } else {
        result = result.concat(" __").concat(String.valueOf(roll)).concat("__");
      }
      keep--;
    }
    result = result.concat(" )");
    return this;
  }
}