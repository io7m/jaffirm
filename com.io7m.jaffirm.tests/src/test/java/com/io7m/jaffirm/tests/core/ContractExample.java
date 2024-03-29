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

package com.io7m.jaffirm.tests.core;

import com.io7m.jaffirm.core.Contracts;
import com.io7m.jaffirm.core.Preconditions;

public final class ContractExample
{
  private ContractExample()
  {

  }

  static int exampleSingles(final int x)
  {
    Preconditions.checkPreconditionI(
      x,
      x > 0,
      i -> "Input " + i + " must be > 0");
    Preconditions.checkPreconditionI(
      x,
      x % 2 == 0,
      i -> "Input " + i + " must be even");
    return x * 2;
  }

  static int exampleMultis(final int x)
  {
    Preconditions.checkPreconditionsI(
      x,
      Contracts.conditionI(i -> i > 0, i -> "Input " + i + " must be > 0"),
      Contracts.conditionI(
        i -> i % 2 == 0,
        i -> "Input " + i + " must be even"));
    return x * 2;
  }

  public static void main(final String[] args)
  {
    exampleMultis(-1);
  }
}
