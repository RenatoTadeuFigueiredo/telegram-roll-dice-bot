package br.com.rtf.roll;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class RollDiceBotUnitTest {

  @SpyBean
  private RollDiceBot rollDiceBot;

  @Test
  public void rollDice_SuperSimpleOne() {
    String result = rollDiceBot.rollDice("/rd20");

    Assert.assertNotEquals("ERROR", result);
    long total = getTotal(result);
    Assert.assertTrue(total >= 1);
    Assert.assertTrue(total <= 20);
  }

  @Test
  public void rollDice_SimpleOne() {
    String result = rollDiceBot.rollDice("/r 1d20");

    Assert.assertNotEquals("ERROR", result);
    long total = getTotal(result);
    Assert.assertTrue(total >= 1);
    Assert.assertTrue(total <= 20);
  }

  @Test
  public void rollDice_SimpleOnePlusNumber() {
    String result = rollDiceBot.rollDice("/r 1d20+15");

    Assert.assertNotEquals("ERROR", result);
    long total = getTotal(result);
    Assert.assertTrue(total >= 16);
    Assert.assertTrue(total <= 35);
  }

  @Test
  public void rollDice_TwoRolls() {
    String result = rollDiceBot.rollDice("/r 1d20+1d6");

    Assert.assertNotEquals("ERROR", result);
    long total = getTotal(result);
    Assert.assertTrue(total >= 2);
    Assert.assertTrue(total <= 26);
  }

  @Test
  public void rollDice_TwoRollsPlusNumber() {
    String result = rollDiceBot.rollDice("/r 1d20+1d6+12");

    Assert.assertNotEquals("ERROR", result);
    long total = getTotal(result);
    Assert.assertTrue(total >= 14);
    Assert.assertTrue(total <= 38);
  }

  @Test
  public void rollDice_AdvancedRoll() {
    String result = rollDiceBot.rollDice("/r 2d20k1");

    Assert.assertNotEquals("ERROR", result);
    long total = getTotal(result);
    Assert.assertTrue(total >= 1);
    Assert.assertTrue(total <= 20);
  }

  @Test
  public void rollDice_DisadvantageRoll() {
    String result = rollDiceBot.rollDice("/r 2d20kl1");

    Assert.assertNotEquals("ERROR", result);
    long total = getTotal(result);
    Assert.assertTrue(total >= 1);
    Assert.assertTrue(total <= 20);
  }

  @Test
  public void rollDice_ReRoll() {
    String result = rollDiceBot.rollDice("/r 4d6r2");

    Assert.assertNotEquals("ERROR", result);
    long total = getTotal(result);
    Assert.assertTrue(total >= 1);
    Assert.assertTrue(total <= 20);
  }

  private long getTotal(String result) {
    int pos = result.indexOf(" = ");
    return Long.parseLong(result.substring(pos + 3));
  }

}