/*
 * Copyright © 2016 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

import java.util.function.DoubleFunction;
import java.util.function.DoublePredicate;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.LongFunction;
import java.util.function.LongPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.io7m.jaffirm.core.SafeApplication.applyDescriberChecked;
import static com.io7m.jaffirm.core.SafeApplication.applyDescriberDChecked;
import static com.io7m.jaffirm.core.SafeApplication.applyDescriberIChecked;
import static com.io7m.jaffirm.core.SafeApplication.applyDescriberLChecked;
import static com.io7m.jaffirm.core.SafeApplication.applySupplierChecked;
import static com.io7m.jaffirm.core.SafeApplication.failedPredicate;
import static com.io7m.jaffirm.core.Violations.innerCheckAll;
import static com.io7m.jaffirm.core.Violations.innerCheckAllDouble;
import static com.io7m.jaffirm.core.Violations.innerCheckAllInt;
import static com.io7m.jaffirm.core.Violations.innerCheckAllLong;
import static com.io7m.jaffirm.core.Violations.singleViolation;

/**
 * Functions to check invariants.
 */

public final class Invariants
{
  private Invariants()
  {
    throw new UnreachableCodeException();
  }

  /**
   * <p>Evaluate all of the given {@code conditions} using {@code value} as
   * input.</p>
   *
   * <p>All of the conditions are evaluated and the function throws {@link
   * InvariantViolationException} if any of the conditions are false, or raise
   * an exception that is not of type {@link Error}. Exceptions of type {@link
   * Error} are propagated immediately, without any further contract
   * checking.</p>
   *
   * @param value      The value
   * @param conditions The set of conditions
   * @param <T>        The type of values
   *
   * @return value
   *
   * @throws InvariantViolationException If any of the conditions are false
   */

  @SafeVarargs
  public static <T> T checkInvariants(
    final T value,
    final ContractConditionType<T>... conditions)
    throws InvariantViolationException
  {
    final Violations violations = innerCheckAll(value, conditions);
    if (violations != null) {
      throw new InvariantViolationException(
        failedMessage(value, violations), null, violations.count());
    }
    return value;
  }

  /**
   * An {@code int} specialized version of {@link #checkInvariants(Object,
   * ContractConditionType[])}
   *
   * @param value      The value
   * @param conditions The conditions the value must obey
   *
   * @return value
   *
   * @throws InvariantViolationException If any of the conditions are false
   */

  public static int checkInvariantsI(
    final int value,
    final ContractIntConditionType... conditions)
    throws InvariantViolationException
  {
    final Violations violations =
      innerCheckAllInt(value, conditions);
    if (violations != null) {
      throw new InvariantViolationException(
        failedMessage(Integer.valueOf(value), violations), null, violations.count());
    }
    return value;
  }

  /**
   * A {@code long} specialized version of {@link #checkInvariants(Object,
   * ContractConditionType[])}
   *
   * @param value      The value
   * @param conditions The conditions the value must obey
   *
   * @return value
   *
   * @throws InvariantViolationException If any of the conditions are false
   */

  public static long checkInvariantsL(
    final long value,
    final ContractLongConditionType... conditions)
    throws InvariantViolationException
  {
    final Violations violations =
      innerCheckAllLong(value, conditions);
    if (violations != null) {
      throw new InvariantViolationException(
        failedMessage(Long.valueOf(value), violations), null, violations.count());
    }
    return value;
  }

  /**
   * A {@code double} specialized version of {@link #checkInvariants(Object,
   * ContractConditionType[])}
   *
   * @param value      The value
   * @param conditions The conditions the value must obey
   *
   * @return value
   *
   * @throws InvariantViolationException If any of the conditions are false
   */

  public static double checkInvariantsD(
    final double value,
    final ContractDoubleConditionType... conditions)
    throws InvariantViolationException
  {
    final Violations violations =
      innerCheckAllDouble(value, conditions);
    if (violations != null) {
      throw new InvariantViolationException(
        failedMessage(Double.valueOf(value), violations), null, violations.count());
    }
    return value;
  }

  /**
   * <p>Evaluate the given {@code predicate} using {@code value} as input.</p>
   *
   * <p>The function throws {@link InvariantViolationException} if the predicate
   * is false.</p>
   *
   * @param value     The value
   * @param condition The predicate
   * @param <T>       The type of values
   *
   * @return value
   *
   * @throws InvariantViolationException If the predicate is false
   */

  public static <T> T checkInvariant(
    final T value,
    final ContractConditionType<T> condition)
    throws InvariantViolationException
  {
    return checkInvariant(value, condition.predicate(), condition.describer());
  }

  /**
   * <p>Evaluate the given {@code predicate} using {@code value} as input.</p>
   *
   * <p>The function throws {@link InvariantViolationException} if the predicate
   * is false.</p>
   *
   * @param value     The value
   * @param predicate The predicate
   * @param describer A describer for the predicate
   * @param <T>       The type of values
   *
   * @return value
   *
   * @throws InvariantViolationException If the predicate is false
   */

  public static <T> T checkInvariant(
    final T value,
    final Predicate<T> predicate,
    final Function<T, String> describer)
  {
    final boolean ok;
    try {
      ok = predicate.test(value);
    } catch (final Throwable e) {
      final Violations violations = singleViolation(failedPredicate(e));
      throw new InvariantViolationException(
        failedMessage(value, violations), e, violations.count());
    }

    return innerCheckInvariant(value, ok, describer);
  }

  /**
   * <p>Evaluate the given {@code predicate} using {@code value} as input.</p>
   *
   * <p>The function throws {@link InvariantViolationException} if the predicate
   * is false.</p>
   *
   * @param value     The value
   * @param condition The predicate
   * @param describer A describer for the predicate
   * @param <T>       The type of values
   *
   * @return value
   *
   * @throws InvariantViolationException If the predicate is false
   */

  public static <T> T checkInvariant(
    final T value,
    final boolean condition,
    final Function<T, String> describer)
  {
    return innerCheckInvariant(value, condition, describer);
  }

  /**
   * A specialized version of {@link #checkInvariant(Object, boolean, Function)}
   * that does not mention an input value.
   *
   * @param condition The predicate
   * @param message   The predicate description
   *
   * @throws InvariantViolationException Iff {@code predicate == false}
   */

  public static void checkInvariant(
    final boolean condition,
    final String message)
    throws InvariantViolationException
  {
    if (!condition) {
      final Violations violations = singleViolation(message);
      throw new InvariantViolationException(
        failedMessage("<unspecified>", violations), null, violations.count());
    }
  }

  /**
   * A specialized version of {@link #checkInvariant(Object, boolean, Function)}
   * that does not mention an input value.
   *
   * @param condition The predicate
   * @param message   The predicate description supplier
   *
   * @throws InvariantViolationException Iff {@code predicate == false}
   */

  public static void checkInvariant(
    final boolean condition,
    final Supplier<String> message)
    throws InvariantViolationException
  {
    if (!condition) {
      final Violations violations = singleViolation(applySupplierChecked(message));
      throw new InvariantViolationException(
        failedMessage("<unspecified>", violations), null, violations.count());
    }
  }

  /**
   * <p>A version of {@link #checkInvariant(boolean, String)} that constructs a
   * description message from the given format string and arguments.</p>
   *
   * <p>Note that the use of variadic arguments may entail allocating memory on
   * virtual machines that fail to eliminate the allocations with <i>escape
   * analysis</i>.</p>
   *
   * @param value     The value
   * @param condition The predicate
   * @param format    The format string
   * @param objects   The format string arguments
   * @param <T>       The precise type of values
   *
   * @return {@code value}
   *
   * @since 1.1.0
   */

  public static <T> T checkInvariantV(
    final T value,
    final boolean condition,
    final String format,
    final Object... objects)
  {
    if (!condition) {
      final Violations violations = singleViolation(String.format(format, objects));
      throw new InvariantViolationException(
        failedMessage(value, violations), null, violations.count());
    }
    return value;
  }

  /**
   * <p>A version of {@link #checkInvariant(boolean, String)} that constructs a
   * description message from the given format string and arguments.</p>
   *
   * <p>Note that the use of variadic arguments may entail allocating memory on
   * virtual machines that fail to eliminate the allocations with <i>escape
   * analysis</i>.</p>
   *
   * @param condition The predicate
   * @param format    The format string
   * @param objects   The format string arguments
   *
   * @since 1.1.0
   */

  public static void checkInvariantV(
    final boolean condition,
    final String format,
    final Object... objects)
  {
    checkInvariantV("<unspecified>", condition, format, objects);
  }

  /**
   * An {@code int} specialized version of {@link #checkInvariant(Object,
   * ContractConditionType)}.
   *
   * @param value     The value
   * @param condition The predicate
   *
   * @return value
   *
   * @throws InvariantViolationException If the predicate is false
   */

  public static int checkInvariantI(
    final int value,
    final ContractIntConditionType condition)
    throws InvariantViolationException
  {
    return checkInvariantI(
      value, condition.predicate(), condition.describer());
  }

  /**
   * An {@code int} specialized version of {@link #checkInvariant(Object,
   * ContractConditionType)}.
   *
   * @param value     The value
   * @param predicate The predicate
   * @param describer The describer for the predicate
   *
   * @return value
   *
   * @throws InvariantViolationException If the predicate is false
   */

  public static int checkInvariantI(
    final int value,
    final IntPredicate predicate,
    final IntFunction<String> describer)
  {
    final boolean ok;
    try {
      ok = predicate.test(value);
    } catch (final Throwable e) {
      final Violations violations = singleViolation(failedPredicate(e));
      throw new InvariantViolationException(
        failedMessage(Integer.valueOf(value), violations), e, violations.count());
    }

    return innerCheckInvariantI(value, ok, describer);
  }

  /**
   * An {@code int} specialized version of {@link #checkInvariant(Object,
   * boolean, Function)}.
   *
   * @param value     The value
   * @param condition The predicate
   * @param describer The describer for the predicate
   *
   * @return value
   *
   * @throws InvariantViolationException If the predicate is false
   */

  public static int checkInvariantI(
    final int value,
    final boolean condition,
    final IntFunction<String> describer)
  {
    return innerCheckInvariantI(value, condition, describer);
  }

  /**
   * A {@code long} specialized version of {@link #checkInvariant(Object,
   * ContractConditionType)}.
   *
   * @param value     The value
   * @param condition The predicate
   *
   * @return value
   *
   * @throws InvariantViolationException If the predicate is false
   */

  public static long checkInvariantL(
    final long value,
    final ContractLongConditionType condition)
    throws InvariantViolationException
  {
    return checkInvariantL(value, condition.predicate(), condition.describer());
  }

  /**
   * A {@code long} specialized version of {@link #checkInvariant(Object,
   * Predicate, Function)}
   *
   * @param value     The value
   * @param predicate The predicate
   * @param describer The describer of the predicate
   *
   * @return value
   *
   * @throws InvariantViolationException If the predicate is false
   */

  public static long checkInvariantL(
    final long value,
    final LongPredicate predicate,
    final LongFunction<String> describer)
  {
    final boolean ok;
    try {
      ok = predicate.test(value);
    } catch (final Throwable e) {
      final Violations violations = singleViolation(failedPredicate(e));
      throw new InvariantViolationException(
        failedMessage(Long.valueOf(value), violations), e, violations.count());
    }

    return innerCheckInvariantL(value, ok, describer);
  }

  /**
   * A {@code long} specialized version of {@link #checkInvariant(Object,
   * Predicate, Function)}
   *
   * @param condition The predicate
   * @param value     The value
   * @param describer The describer of the predicate
   *
   * @return value
   *
   * @throws InvariantViolationException If the predicate is false
   */

  public static long checkInvariantL(
    final long value,
    final boolean condition,
    final LongFunction<String> describer)
  {
    return innerCheckInvariantL(value, condition, describer);
  }

  /**
   * A {@code double} specialized version of {@link #checkInvariant(Object,
   * ContractConditionType)}.
   *
   * @param value     The value
   * @param condition The predicate
   *
   * @return value
   *
   * @throws InvariantViolationException If the predicate is false
   */

  public static double checkInvariantD(
    final double value,
    final ContractDoubleConditionType condition)
    throws InvariantViolationException
  {
    return checkInvariantD(value, condition.predicate(), condition.describer());
  }

  /**
   * A {@code double} specialized version of {@link #checkInvariant(Object,
   * Predicate, Function)}
   *
   * @param value     The value
   * @param predicate The predicate
   * @param describer The describer of the predicate
   *
   * @return value
   *
   * @throws InvariantViolationException If the predicate is false
   */

  public static double checkInvariantD(
    final double value,
    final DoublePredicate predicate,
    final DoubleFunction<String> describer)
  {
    final boolean ok;
    try {
      ok = predicate.test(value);
    } catch (final Throwable e) {
      final Violations violations = singleViolation(failedPredicate(e));
      throw new InvariantViolationException(
        failedMessage(Double.valueOf(value), violations), e, violations.count());
    }

    return innerCheckInvariantD(value, ok, describer);
  }

  /**
   * A {@code double} specialized version of {@link #checkInvariant(Object,
   * boolean, Function)}
   *
   * @param value     The value
   * @param condition The predicate
   * @param describer The describer of the predicate
   *
   * @return value
   *
   * @throws InvariantViolationException If the predicate is false
   */

  public static double checkInvariantD(
    final double value,
    final boolean condition,
    final DoubleFunction<String> describer)
  {
    return innerCheckInvariantD(value, condition, describer);
  }

  private static <T> T innerCheckInvariant(
    final T value,
    final boolean condition,
    final Function<T, String> describer)
  {
    if (!condition) {
      final Violations violations = singleViolation(applyDescriberChecked(value, describer));
      throw new InvariantViolationException(
        failedMessage(value, violations), null, violations.count());
    }
    return value;
  }

  private static double innerCheckInvariantD(
    final double value,
    final boolean condition,
    final DoubleFunction<String> describer)
  {
    if (!condition) {
      final Violations violations = singleViolation(applyDescriberDChecked(value, describer));
      throw new InvariantViolationException(
        failedMessage(Double.valueOf(value), violations), null, violations.count());
    }
    return value;
  }

  private static long innerCheckInvariantL(
    final long value,
    final boolean condition,
    final LongFunction<String> describer)
  {
    if (!condition) {
      final Violations violations = singleViolation(applyDescriberLChecked(value, describer));
      throw new InvariantViolationException(
        failedMessage(Long.valueOf(value), violations), null, violations.count());
    }
    return value;
  }

  private static int innerCheckInvariantI(
    final int value,
    final boolean condition,
    final IntFunction<String> describer)
  {
    if (!condition) {
      final Violations violations = singleViolation(applyDescriberIChecked(value, describer));
      throw new InvariantViolationException(
        failedMessage(Integer.valueOf(value), violations), null, violations.count());
    }
    return value;
  }

  private static <T> String failedMessage(
    final T value,
    final Violations violations)
  {
    final String line_separator = System.lineSeparator();
    final StringBuilder sb = new StringBuilder(128);
    sb.append("Invariant violation.");
    sb.append(line_separator);

    sb.append("  Received: ");
    sb.append(value);
    sb.append(line_separator);

    sb.append("  Violated conditions: ");
    sb.append(line_separator);

    final String[] messages = violations.messages();
    for (int index = 0; index < messages.length; ++index) {
      if (messages[index] != null) {
        sb.append("    [");
        sb.append(index);
        sb.append("]: ");
        sb.append(messages[index]);
        sb.append(line_separator);
      }
    }
    return sb.toString();
  }
}
