package br.com.rtf.roll.process;

public interface Process {

  Process invoke();

  String getResult();

  long getTotal();
}
