/*
 * Copyright © 2016 <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.jaffirm.core;

import com.io7m.junreachable.UnreachableCodeException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.function.DoubleFunction;
import java.util.function.DoublePredicate;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.LongFunction;
import java.util.function.LongPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Functions to check preconditions.
 */

public final class Preconditions
{
  private Preconditions()
  {
    throw new UnreachableCodeException();
  }

  /**
   * <p>Evaluate all of the given {@code conditions} using {@code value} as
   * input.</p>
   *
   * <p>All of the conditions are evaluated and the function throws {@link
   * PreconditionViolationException} if any of the conditions are false, or
   * raise an exception that is not of type {@link Error}. Exceptions of type
   * {@link Error} are propagated immediately, without any further contract
   * checking.</p>
   *
   * @param value      The value
   * @param conditions The set of conditions
   * @param <T>        The type of values
   *
   * @return value
   *
   * @throws PreconditionViolationException If any of the conditions are false
   */

  @SafeVarargs
  public static <T> T checkPreconditions(
    final T value,
    final ContractConditionType<T>... conditions)
    throws PreconditionViolationException
  {
    final Violations violations = innerCheckAll(value, conditions);
    if (violations != null) {
      throw preconditionsFailed(value, Optional.empty(), violations);
    }
    return value;
  }

  /**
   * An {@code int} specialized version of {@link #checkPreconditions(Object,
   * ContractConditionType[])}
   *
   * @param value      The value
   * @param conditions The conditions the value must obey
   *
   * @return value
   *
   * @throws PreconditionViolationException If any of the conditions are false
   */

  public static int checkPreconditionsI(
    final int value,
    final ContractIntConditionType... conditions)
    throws PreconditionViolationException
  {
    final Violations violations = innerCheckAllInt(value, conditions);
    if (violations != null) {
      throw preconditionsFailed(
        Integer.valueOf(value), Optional.empty(), violations);
    }
    return value;
  }

  /**
   * A {@code long} specialized version of {@link #checkPreconditions(Object,
   * ContractConditionType[])}
   *
   * @param value      The value
   * @param conditions The conditions the value must obey
   *
   * @return value
   *
   * @throws PreconditionViolationException If any of the conditions are false
   */

  public static long checkPreconditionsL(
    final long value,
    final ContractLongConditionType... conditions)
    throws PreconditionViolationException
  {
    final Violations violations = innerCheckAllLong(value, conditions);
    if (violations != null) {
      throw preconditionsFailed(
        Long.valueOf(value), Optional.empty(), violations);
    }
    return value;
  }

  /**
   * A {@code double} specialized version of {@link #checkPreconditions(Object,
   * ContractConditionType[])}
   *
   * @param value      The value
   * @param conditions The conditions the value must obey
   *
   * @return value
   *
   * @throws PreconditionViolationException If any of the conditions are false
   */

  public static double checkPreconditionsD(
    final double value,
    final ContractDoubleConditionType... conditions)
    throws PreconditionViolationException
  {
    final Violations violations = innerCheckAllDouble(value, conditions);
    if (violations != null) {
      throw preconditionsFailed(
        Double.valueOf(value), Optional.empty(), violations);
    }
    return value;
  }

  /**
   * <p>Evaluate the given {@code predicate} using {@code value} as input.</p>
   *
   * <p>The function throws {@link PreconditionViolationException} if the
   * predicate is false.</p>
   *
   * @param value     The value
   * @param condition The predicate
   * @param <T>       The type of values
   *
   * @return value
   *
   * @throws PreconditionViolationException If the predicate is false
   */

  public static <T> T checkPrecondition(
    final T value,
    final ContractConditionType<T> condition)
    throws PreconditionViolationException
  {
    return checkPrecondition(
      value, condition.predicate(), condition.describer());
  }

  /**
   * <p>Evaluate the given {@code predicate} using {@code value} as input.</p>
   *
   * <p>The function throws {@link PreconditionViolationException} if the
   * predicate is false.</p>
   *
   * @param value     The value
   * @param predicate The predicate
   * @param describer A describer for the predicate
   * @param <T>       The type of values
   *
   * @return value
   *
   * @throws PreconditionViolationException If the predicate is false
   */

  public static <T> T checkPrecondition(
    final T value,
    final Predicate<T> predicate,
    final Function<T, String> describer)
  {
    final boolean ok;
    try {
      ok = predicate.test(value);
    } catch (final Throwable e) {
      throw preconditionsFailed(
        value, Optional.empty(), Violations.one(failedPredicate(e)));
    }

    return innerCheck(value, ok, describer);
  }

  /**
   * <p>Evaluate the given {@code predicate} using {@code value} as input.</p>
   *
   * <p>The function throws {@link PreconditionViolationException} if the
   * predicate is false.</p>
   *
   * @param value     The value
   * @param condition The predicate
   * @param describer A describer for the predicate
   * @param <T>       The type of values
   *
   * @return value
   *
   * @throws PreconditionViolationException If the predicate is false
   */

  public static <T> T checkPrecondition(
    final T value,
    final boolean condition,
    final Function<T, String> describer)
  {
    return innerCheck(value, condition, describer);
  }

  /**
   * A specialized version of {@link #checkPrecondition(Object, boolean,
   * Function)} that does not mention an input value.
   *
   * @param condition The predicate
   * @param message   The predicate description
   *
   * @throws PreconditionViolationException Iff {@code predicate == false}
   */

  public static void checkPrecondition(
    final boolean condition,
    final String message)
    throws PreconditionViolationException
  {
    if (!condition) {
      throw preconditionsFailed(
        "<unspecified>", Optional.empty(), Violations.one(message));
    }
  }

  /**
   * A specialized version of {@link #checkPrecondition(Object, boolean,
   * Function)} that does not mention an input value.
   *
   * @param condition The predicate
   * @param message   The predicate description supplier
   *
   * @throws PreconditionViolationException Iff {@code predicate == false}
   */

  public static void checkPrecondition(
    final boolean condition,
    final Supplier<String> message)
    throws PreconditionViolationException
  {
    if (!condition) {
      throw preconditionsFailed(
        "<unspecified>",
        Optional.empty(),
        Violations.one(applySupplierChecked(message)));
    }
  }

  /**
   * An {@code int} specialized version of {@link #checkPrecondition(Object,
   * ContractConditionType)}.
   *
   * @param value     The value
   * @param condition The predicate
   *
   * @return value
   *
   * @throws PreconditionViolationException If the predicate is false
   */

  public static int checkPreconditionI(
    final int value,
    final ContractIntConditionType condition)
    throws PreconditionViolationException
  {
    return checkPreconditionI(
      value, condition.predicate(), condition.describer());
  }

  /**
   * An {@code int} specialized version of {@link #checkPrecondition(Object,
   * ContractConditionType)}.
   *
   * @param value     The value
   * @param predicate The predicate
   * @param describer The describer for the predicate
   *
   * @return value
   *
   * @throws PreconditionViolationException If the predicate is false
   */

  public static int checkPreconditionI(
    final int value,
    final IntPredicate predicate,
    final IntFunction<String> describer)
  {
    final boolean ok;
    try {
      ok = predicate.test(value);
    } catch (final Throwable e) {
      throw preconditionsFailed(
        Integer.valueOf(value),
        Optional.empty(),
        Violations.one(failedPredicate(e)));
    }

    return innerCheckI(value, ok, describer);
  }

  /**
   * An {@code int} specialized version of {@link #checkPrecondition(Object,
   * boolean, Function)}.
   *
   * @param value     The value
   * @param condition The predicate
   * @param describer The describer for the predicate
   *
   * @return value
   *
   * @throws PreconditionViolationException If the predicate is false
   */

  public static int checkPreconditionI(
    final int value,
    final boolean condition,
    final IntFunction<String> describer)
  {
    return innerCheckI(value, condition, describer);
  }

  /**
   * A {@code long} specialized version of {@link #checkPrecondition(Object,
   * ContractConditionType)}.
   *
   * @param value     The value
   * @param condition The predicate
   *
   * @return value
   *
   * @throws PreconditionViolationException If the predicate is false
   */

  public static long checkPreconditionL(
    final long value,
    final ContractLongConditionType condition)
    throws PreconditionViolationException
  {
    return checkPreconditionL(
      value, condition.predicate(), condition.describer());
  }

  /**
   * A {@code long} specialized version of {@link #checkPrecondition(Object,
   * Predicate, Function)}
   *
   * @param value     The value
   * @param predicate The predicate
   * @param describer The describer of the predicate
   *
   * @return value
   *
   * @throws PreconditionViolationException If the predicate is false
   */

  public static long checkPreconditionL(
    final long value,
    final LongPredicate predicate,
    final LongFunction<String> describer)
  {
    final boolean ok;
    try {
      ok = predicate.test(value);
    } catch (final Throwable e) {
      throw preconditionsFailed(
        Long.valueOf(value),
        Optional.empty(),
        Violations.one(failedPredicate(e)));
    }

    return innerCheckL(value, ok, describer);
  }

  /**
   * A {@code long} specialized version of {@link #checkPrecondition(Object,
   * Predicate, Function)}
   *
   * @param condition The predicate
   * @param value     The value
   * @param describer The describer of the predicate
   *
   * @return value
   *
   * @throws PreconditionViolationException If the predicate is false
   */

  public static long checkPreconditionL(
    final long value,
    final boolean condition,
    final LongFunction<String> describer)
  {
    return innerCheckL(value, condition, describer);
  }

  /**
   * A {@code double} specialized version of {@link #checkPrecondition(Object,
   * ContractConditionType)}.
   *
   * @param value     The value
   * @param condition The predicate
   *
   * @return value
   *
   * @throws PreconditionViolationException If the predicate is false
   */

  public static double checkPreconditionD(
    final double value,
    final ContractDoubleConditionType condition)
    throws PreconditionViolationException
  {
    return checkPreconditionD(
      value, condition.predicate(), condition.describer());
  }

  /**
   * A {@code double} specialized version of {@link #checkPrecondition(Object,
   * Predicate, Function)}
   *
   * @param value     The value
   * @param predicate The predicate
   * @param describer The describer of the predicate
   *
   * @return value
   *
   * @throws PreconditionViolationException If the predicate is false
   */

  public static double checkPreconditionD(
    final double value,
    final DoublePredicate predicate,
    final DoubleFunction<String> describer)
  {
    final boolean ok;
    try {
      ok = predicate.test(value);
    } catch (final Throwable e) {
      throw preconditionsFailed(
        Double.valueOf(value),
        Optional.empty(),
        Violations.one(failedPredicate(e)));
    }

    return innerCheckD(value, ok, describer);
  }

  /**
   * A {@code double} specialized version of {@link #checkPrecondition(Object,
   * boolean, Function)}
   *
   * @param value     The value
   * @param condition The predicate
   * @param describer The describer of the predicate
   *
   * @return value
   *
   * @throws PreconditionViolationException If the predicate is false
   */

  public static double checkPreconditionD(
    final double value,
    final boolean condition,
    final DoubleFunction<String> describer)
  {
    return innerCheckD(value, condition, describer);
  }

  private static <T> T innerCheck(
    final T value,
    final boolean condition,
    final Function<T, String> describer)
  {
    if (!condition) {
      throw preconditionsFailed(
        value,
        Optional.empty(),
        Violations.one(applyDescriberChecked(value, describer)));
    }
    return value;
  }

  private static int innerCheckI(
    final int value,
    final boolean condition,
    final IntFunction<String> describer)
  {
    if (!condition) {
      throw preconditionsFailed(
        Integer.valueOf(value),
        Optional.empty(),
        Violations.one(applyDescriberIChecked(value, describer)));
    }
    return value;
  }

  private static long innerCheckL(
    final long value,
    final boolean condition,
    final LongFunction<String> describer)
  {
    if (!condition) {
      throw preconditionsFailed(
        Long.valueOf(value),
        Optional.empty(),
        Violations.one(applyDescriberLChecked(value, describer)));
    }
    return value;
  }

  private static double innerCheckD(
    final double value,
    final boolean condition,
    final DoubleFunction<String> describer)
  {
    if (!condition) {
      throw preconditionsFailed(
        Double.valueOf(value),
        Optional.empty(),
        Violations.one(applyDescriberDChecked(value, describer)));
    }
    return value;
  }

  private static <T> Violations innerCheckAll(
    final T value,
    final ContractConditionType<T>[] conditions)
  {
    Violations violations = null;

    for (int index = 0; index < conditions.length; ++index) {
      final ContractConditionType<T> condition = conditions[index];
      final Predicate<T> predicate = condition.predicate();

      final boolean ok;
      final int count = conditions.length;
      try {
        ok = predicate.test(value);
      } catch (final Throwable e) {
        violations = maybeAllocate(violations, count);
        violations.messages[index] = failedPredicate(e);
        ++violations.count;
        continue;
      }

      if (!ok) {
        violations = maybeAllocate(violations, count);
        violations.messages[index] =
          applyDescriberChecked(value, condition.describer());
        ++violations.count;
      }
    }
    return violations;
  }

  private static Violations innerCheckAllInt(
    final int value,
    final ContractIntConditionType[] conditions)
  {
    Violations violations = null;

    for (int index = 0; index < conditions.length; ++index) {
      final ContractIntConditionType condition = conditions[index];
      final IntPredicate predicate = condition.predicate();

      final boolean ok;
      final int count = conditions.length;
      try {
        ok = predicate.test(value);
      } catch (final Throwable e) {
        violations = maybeAllocate(violations, count);
        violations.messages[index] = failedPredicate(e);
        ++violations.count;
        continue;
      }

      if (!ok) {
        violations = maybeAllocate(violations, count);
        violations.messages[index] =
          applyDescriberIChecked(value, condition.describer());
        ++violations.count;
      }
    }
    return violations;
  }

  private static Violations innerCheckAllLong(
    final long value,
    final ContractLongConditionType[] conditions)
  {
    Violations violations = null;

    for (int index = 0; index < conditions.length; ++index) {
      final ContractLongConditionType condition = conditions[index];
      final LongPredicate predicate = condition.predicate();

      final boolean ok;
      final int count = conditions.length;
      try {
        ok = predicate.test(value);
      } catch (final Throwable e) {
        violations = maybeAllocate(violations, count);
        violations.messages[index] = failedPredicate(e);
        ++violations.count;
        continue;
      }

      if (!ok) {
        violations = maybeAllocate(violations, count);
        violations.messages[index] =
          applyDescriberLChecked(value, condition.describer());
        ++violations.count;
      }
    }
    return violations;
  }

  private static Violations innerCheckAllDouble(
    final double value,
    final ContractDoubleConditionType[] conditions)
  {
    Violations violations = null;

    for (int index = 0; index < conditions.length; ++index) {
      final ContractDoubleConditionType condition = conditions[index];
      final DoublePredicate predicate = condition.predicate();

      final boolean ok;
      final int count = conditions.length;
      try {
        ok = predicate.test(value);
      } catch (final Throwable e) {
        violations = maybeAllocate(violations, count);
        violations.messages[index] = failedPredicate(e);
        ++violations.count;
        continue;
      }

      if (!ok) {
        violations = maybeAllocate(violations, count);
        violations.messages[index] =
          applyDescriberDChecked(value, condition.describer());
        ++violations.count;
      }
    }
    return violations;
  }

  private static Violations maybeAllocate(
    final Violations violations,
    final int count)
  {
    return violations == null ? new Violations(count) : violations;
  }

  private static String applySupplierChecked(
    final Supplier<String> message)
  {
    try {
      return message.get();
    } catch (final Throwable e) {
      return failedDescriber(e);
    }
  }

  private static <T> String applyDescriberChecked(
    final T value,
    final Function<T, String> describer)
  {
    try {
      return describer.apply(value);
    } catch (final Throwable e) {
      return failedDescriber(e);
    }
  }

  private static String applyDescriberIChecked(
    final int value,
    final IntFunction<String> describer)
  {
    try {
      return describer.apply(value);
    } catch (final Throwable e) {
      return failedDescriber(e);
    }
  }

  private static String applyDescriberLChecked(
    final long value,
    final LongFunction<String> describer)
  {
    try {
      return describer.apply(value);
    } catch (final Throwable e) {
      return failedDescriber(e);
    }
  }

  private static String applyDescriberDChecked(
    final double value,
    final DoubleFunction<String> describer)
  {
    try {
      return describer.apply(value);
    } catch (final Throwable e) {
      return failedDescriber(e);
    }
  }

  private static <T> String failedPredicate(
    final Throwable exception)
  {
    return failedApply(
      exception, "Exception raised whilst evaluating predicate: ");
  }

  private static String failedDescriber(
    final Throwable exception)
  {
    return failedApply(
      exception, "Exception raised whilst evaluating describer: ");
  }

  private static String failedApply(
    final Throwable exception,
    final String prefix)
  {
    if (exception instanceof Error) {
      throw (Error) exception;
    }

    final StringBuilder sb = new StringBuilder(128);
    sb.append(prefix);
    sb.append(exception.getClass());
    sb.append(": ");
    sb.append(exception.getMessage());
    sb.append(System.lineSeparator());
    sb.append(System.lineSeparator());
    stackTraceToStringBuilder(exception, sb);
    return sb.toString();
  }

  private static void stackTraceToStringBuilder(
    final Throwable exception,
    final StringBuilder sb)
  {
    final StringWriter sw = new StringWriter();
    final PrintWriter pw = new PrintWriter(sw, true);
    exception.printStackTrace(pw);
    sb.append(sw.toString());
  }

  private static <T> PreconditionViolationException preconditionsFailed(
    final T value,
    final Optional<T> expected,
    final Violations violations)
  {
    final StringBuilder sb = new StringBuilder(128);
    sb.append("Precondition violation.");
    sb.append(System.lineSeparator());

    sb.append("  Received: ");
    sb.append(value);
    sb.append(System.lineSeparator());

    if (expected.isPresent()) {
      sb.append("  Expected: ");
      sb.append(expected.get());
      sb.append(System.lineSeparator());
    }

    sb.append("  Violated conditions: ");
    sb.append(System.lineSeparator());

    final String[] messages = violations.messages;
    for (int index = 0; index < messages.length; ++index) {
      if (messages[index] != null) {
        sb.append("    [");
        sb.append(index);
        sb.append("]: ");
        sb.append(messages[index]);
        sb.append(System.lineSeparator());
      }
    }

    throw new PreconditionViolationException(sb.toString(), violations.count);
  }

  private static final class Violations
  {
    private final String[] messages;
    private int count;

    private Violations(final int expected)
    {
      this.messages = new String[expected];
      this.count = 0;
    }

    private static Violations one(
      final String message)
    {
      final Violations violations = new Violations(1);
      violations.messages[0] = message;
      violations.count = 1;
      return violations;
    }
  }
}
