package br.com.rtf.roll.listener;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.rtf.roll.config.RollProperties;
import br.com.rtf.roll.process.Process;
import br.com.rtf.roll.process.ProcessKeep;
import br.com.rtf.roll.process.ProcessReroll;
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

  private final RollProperties properties;

  private Random random;

  public RollDiceBot(RollProperties properties) {
    this.properties = properties;
  }

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
    return properties.getUsername();
  }

  @Override
  public String getBotToken() {
    return properties.getToken();
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
          } else {
            Process process = null;
            switch (role) {
              case "k":
                rolls.sort(Comparator.reverseOrder());
                process = new ProcessKeep(result, total, m.group(4), rolls).invoke();
                break;
              case "kl":
                rolls.sort(Comparator.naturalOrder());
                process = new ProcessKeep(result, total, m.group(4), rolls).invoke();
                break;
              case "r":
                rolls.sort(Comparator.reverseOrder());
                process = new ProcessReroll(result, total, m.group(4), rolls, sides).invoke();
                break;
            }

            if (process == null) {
              LOGGER.error("FAIL TO RECOGNIZE [{}]", role);
              return "ERROR";
            }

            result = process.getResult();
            total = process.getTotal();
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


}