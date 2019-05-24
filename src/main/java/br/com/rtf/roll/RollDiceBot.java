package br.com.rtf.roll;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Slf4j
public class RollDiceBot extends TelegramLongPollingBot {

  private Random random;

  @Override
  public void onUpdateReceived(Update update) {
    onUpdatesReceived(Collections.singletonList(update));
  }

  @Override
  public void onUpdatesReceived(List<Update> updates) {
    updates.forEach(update -> {
      SendMessage response = new SendMessage();
      Message message = update.getMessage();

      String returnMessage = rollDice(message.getText());
      if (returnMessage != null) {
        response.setText(returnMessage);
        response.setChatId(message.getChatId());
        response.setReplyToMessageId(message.getMessageId());
        try {
          execute(response);
        } catch (TelegramApiException e) {
          e.printStackTrace();
        }
      }
    });
  }

  @Override
  public String getBotUsername() {
    return "java_roll_dice_bot";
  }

  @Override
  public String getBotToken() {
    return "894822534:AAE1ze6s887NL8W1bYWphKltk0QhRf_oPfU";
  }

  protected String rollDice(String message) {
    message = message.toLowerCase();
    LOGGER.debug("TRY TO ROLL => " + message);

    if (message.startsWith("/roll")) {
      message = message.substring(5).trim();
    } else if (message.startsWith("/r")) {
      message = message.substring(2).trim();
    } else {
      LOGGER.error("FAIL TO IDENTIFY [{}]", message);
      return null;
    }

    String result = "";
    long total = 0L;
    random = new Random();

    String[] steps = message.split("\\+");
    for (String step : steps) {
      if (StringUtils.isNumeric(step)) {
        total = total + Long.valueOf(step);
        result = addRollOnResult(result, Integer.parseInt(step));
      } else {
        Pattern p = Pattern.compile("([1-9]\\d*)?d([1-9]\\d*)?(k|kl|r)?([1-9]?)");
        Matcher m = p.matcher(step);
        if (m.matches()) {
          String num = m.group(1);
          if (Strings.isBlank(num)) {
            num = "1";
          }

          int sides = Integer.parseInt(m.group(2));

          List<Integer> rolls = new CopyOnWriteArrayList<>();
          for (int i = 0; i < Integer.parseInt(num); i++) {
            rolls.add(random.nextInt(sides) + 1);
          }

          String role = m.group(3);
          if (role == null) {
            for (Integer roll : rolls) {
              total = total + roll;
              result = addRollOnResult(result, roll);
            }
          } else if (role.equals("k")) {
            rolls.sort(Comparator.reverseOrder());
            ProcessKeep processKeep = new ProcessKeep(result, total, m.group(4), rolls).invoke();
            result = processKeep.getResult();
            total = processKeep.getTotal();
          } else if (role.equals("kl")) {
            rolls.sort(Comparator.naturalOrder());
            ProcessKeep processKeep = new ProcessKeep(result, total, m.group(4), rolls).invoke();
            result = processKeep.getResult();
            total = processKeep.getTotal();
          } else if (role.equals("r")) {
            rolls.sort(Comparator.reverseOrder());
            ProcessReroll processReroll = new ProcessReroll(result, total, m.group(4), rolls, sides).invoke();
            result = processReroll.getResult();
            total = processReroll.getTotal();
          }
        } else {
          LOGGER.error("FAIL TO CONVERT [{}]", step);
          return "ERROR";
        }
      }
    }

    if (total > 0) {
      return result.concat(" = ").concat(String.valueOf(total));
    }

    return "ERROR";
  }

  private String addRollOnResult(String result, int roll) {
    if (result.length() > 0) {
      result = result.concat(" + ");
    }

    return result.concat("(" + roll + ")");
  }

  private class ProcessKeep {
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

    public String getResult() {
      return result;
    }

    public long getTotal() {
      return total;
    }

    public ProcessKeep invoke() {
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

  private class ProcessReroll {
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

    public String getResult() {
      return result;
    }

    public long getTotal() {
      return total;
    }

    public ProcessReroll invoke() {
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
}