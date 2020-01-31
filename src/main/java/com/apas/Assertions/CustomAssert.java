package com.apas.Assertions;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.testng.asserts.IAssert;
import org.testng.asserts.IAssertLifecycle;

public class CustomAssert implements IAssertLifecycle {

	protected void doAssert(IAssert<?> assertCommand) {
		onBeforeAssert(assertCommand);
		try {
			executeAssert(assertCommand);
			onAssertSuccess(assertCommand);
		} catch (AssertionError ex) {
			onAssertFailure(assertCommand, ex);
			throw ex;
		} finally {
			onAfterAssert(assertCommand);
		}
	}

	/**
	 * Run the assert command in parameter. Meant to be overridden by
	 * subclasses.
	 */
	@Override
	public void executeAssert(IAssert<?> assertCommand) {
		assertCommand.doAssert();
	}

	/**
	 * Invoked when an assert succeeds. Meant to be overridden by subclasses.
	 */
	@Override
	public void onAssertSuccess(IAssert<?> assertCommand) {
	}

	/**
	 * Invoked when an assert fails. Meant to be overridden by subclasses.
	 * 
	 * @deprecated use onAssertFailure(IAssert assertCommand, AssertionError ex)
	 *             instead of.
	 */
	@Deprecated
	@Override
	public void onAssertFailure(IAssert<?> assertCommand) {
	}

	@Override
	public void onAssertFailure(IAssert<?> assertCommand, AssertionError ex) {
		onAssertFailure(assertCommand);
	}

	/**
	 * Invoked before an assert is run. Meant to be overridden by subclasses.
	 */
	@Override
	public void onBeforeAssert(IAssert<?> assertCommand) {
	}

	/**
	 * Invoked after an assert is run. Meant to be overridden by subclasses.
	 */
	@Override
	public void onAfterAssert(IAssert<?> assertCommand) {
	}

	abstract private static class SimpleAssert<T> implements IAssert<T> {
		private final T actual;
		private final T expected;
		private final String m_message;

		public SimpleAssert(String message) {
			this(null, null, message);
		}

		public SimpleAssert(T actual, T expected) {
			this(actual, expected, null);
		}

		public SimpleAssert(T actual, T expected, String message) {
			this.actual = actual;
			this.expected = expected;
			m_message = message;
		}

		@Override
		public String getMessage() {
			return m_message;
		}

		@Override
		public T getActual() {
			return actual;
		}

		@Override
		public T getExpected() {
			return expected;
		}

		@Override
		abstract public void doAssert();
	}

	public void assertTrue(final boolean condition, final String message) {
		doAssert(new SimpleAssert<Boolean>(condition, Boolean.TRUE, message) {
			@Override
			public void doAssert() {
				com.apas.Assertions.CustomHardAssert.assertTrue(condition, message);
			}
		});
	}

	public void assertFalse(final boolean condition, final String message) {
		doAssert(new SimpleAssert<Boolean>(condition, Boolean.FALSE, message) {
			@Override
			public void doAssert() {
				com.apas.Assertions.CustomHardAssert.assertFalse(condition, message);
			}
		});
	}

	public void fail(final String message) {
		doAssert(new SimpleAssert<Object>(message) {
			@Override
			public void doAssert() {
				com.apas.Assertions.CustomHardAssert.fail(message);
			}
		});
	}

	public <T> void assertEquals(final T actual, final T expected, final String message) {
		doAssert(new SimpleAssert<T>(actual, expected, message) {
			@Override
			public void doAssert() {
				com.apas.Assertions.CustomHardAssert.assertEquals(actual, expected, message);
			}
		});
	}

	public void assertEquals(final String actual, final String expected, final String message) {
		doAssert(new SimpleAssert<String>(actual, expected, message) {
			@Override
			public void doAssert() {
				com.apas.Assertions.CustomHardAssert.assertEquals(actual, expected, message);
			}
		});
	}

	public void assertEquals(final double actual, final double expected, final double delta, final String message) {
		doAssert(new SimpleAssert<Double>(actual, expected, message) {
			@Override
			public void doAssert() {
				com.apas.Assertions.CustomHardAssert.assertEquals(actual, expected, delta, message);
			}
		});
	}

	public void assertEquals(final float actual, final float expected, final float delta, final String message) {
		doAssert(new SimpleAssert<Float>(actual, expected, message) {
			@Override
			public void doAssert() {
				com.apas.Assertions.CustomHardAssert.assertEquals(actual, expected, delta, message);
			}
		});
	}

	public void assertEquals(final long actual, final long expected, final String message) {
		doAssert(new SimpleAssert<Long>(actual, expected, message) {
			@Override
			public void doAssert() {
				com.apas.Assertions.CustomHardAssert.assertEquals(actual, expected, message);
			}
		});
	}

	public void assertEquals(final boolean actual, final boolean expected, final String message) {
		doAssert(new SimpleAssert<Boolean>(actual, expected, message) {
			@Override
			public void doAssert() {
				com.apas.Assertions.CustomHardAssert.assertEquals(actual, expected, message);
			}
		});
	}

	public void assertEquals(final byte actual, final byte expected, final String message) {
		doAssert(new SimpleAssert<Byte>(actual, expected, message) {
			@Override
			public void doAssert() {
				com.apas.Assertions.CustomHardAssert.assertEquals(actual, expected, message);
			}
		});
	}

	public void assertEquals(final char actual, final char expected, final String message) {
		doAssert(new SimpleAssert<Character>(actual, expected, message) {
			@Override
			public void doAssert() {
				com.apas.Assertions.CustomHardAssert.assertEquals(actual, expected, message);
			}
		});
	}

	public void assertEquals(final short actual, final short expected, final String message) {
		doAssert(new SimpleAssert<Short>(actual, expected, message) {
			@Override
			public void doAssert() {
				com.apas.Assertions.CustomHardAssert.assertEquals(actual, expected, message);
			}
		});
	}

	public void assertEquals(final int actual, final int expected, final String message) {
		doAssert(new SimpleAssert<Integer>(actual, expected, message) {
			@Override
			public void doAssert() {
				com.apas.Assertions.CustomHardAssert.assertEquals(actual, expected, message);
			}
		});
	}

	public void assertNotNull(final Object object, final String message) {
		doAssert(new SimpleAssert<Object>(object, null, message) {
			@Override
			public void doAssert() {
				com.apas.Assertions.CustomHardAssert.assertNotNull(object, message);
			}
		});
	}

	public void assertNull(final Object object, final String message) {
		doAssert(new SimpleAssert<Object>(object, null, message) {
			@Override
			public void doAssert() {
				com.apas.Assertions.CustomHardAssert.assertNull(object, message);
			}
		});
	}

	public void assertSame(final Object actual, final Object expected, final String message) {
		doAssert(new SimpleAssert<Object>(actual, expected, message) {
			@Override
			public void doAssert() {
				com.apas.Assertions.CustomHardAssert.assertSame(actual, expected, message);
			}
		});
	}

	public void assertNotSame(final Object actual, final Object expected, final String message) {
		doAssert(new SimpleAssert<Object>(actual, expected, message) {
			@Override
			public void doAssert() {
				com.apas.Assertions.CustomHardAssert.assertNotSame(actual, expected, message);
			}
		});
	}

	public void assertEquals(final Collection<?> actual, final Collection<?> expected, final String message) {
		doAssert(new SimpleAssert<Collection<?>>(actual, expected, message) {
			@Override
			public void doAssert() {
				com.apas.Assertions.CustomHardAssert.assertEquals(actual, expected, message);
			}
		});
	}

	public void assertEquals(final Object[] actual, final Object[] expected, final String message) {
		doAssert(new SimpleAssert<Object[]>(actual, expected, message) {
			@Override
			public void doAssert() {
				com.apas.Assertions.CustomHardAssert.assertEquals(actual, expected, message);
			}
		});
	}

	public void assertEqualsNoOrder(final Object[] actual, final Object[] expected, final String message) {
		doAssert(new SimpleAssert<Object[]>(actual, expected, message) {
			@Override
			public void doAssert() {
				com.apas.Assertions.CustomHardAssert.assertEqualsNoOrder(actual, expected, message);
			}
		});
	}

	public void assertEquals(final byte[] actual, final byte[] expected, final String message) {
		doAssert(new SimpleAssert<byte[]>(actual, expected, message) {
			@Override
			public void doAssert() {
				com.apas.Assertions.CustomHardAssert.assertEquals(actual, expected, message);
			}
		});
	}

	public void assertEquals(final Set<?> actual, final Set<?> expected, final String message) {
		doAssert(new SimpleAssert<Set<?>>(actual, expected, message) {
			@Override
			public void doAssert() {
				com.apas.Assertions.CustomHardAssert.assertEquals(actual, expected, message);
			}
		});
	}

	public void assertEquals(final Map<?, ?> actual, final Map<?, ?> expected, final String message) {
		doAssert(new SimpleAssert<Map<?, ?>>(actual, expected) {
			@Override
			public void doAssert() {
				com.apas.Assertions.CustomHardAssert.assertEquals(actual, expected, message);
			}
		});
	}

	public void assertNotEquals(final Object actual, final Object expected, final String message) {
		doAssert(new SimpleAssert<Object>(actual, expected, message) {
			@Override
			public void doAssert() {
				com.apas.Assertions.CustomHardAssert.assertNotEquals(actual, expected, message);
			}
		});
	}

	public void assertNotEquals(final String actual, final String expected, final String message) {
		doAssert(new SimpleAssert<String>(actual, expected, message) {
			@Override
			public void doAssert() {
				com.apas.Assertions.CustomHardAssert.assertNotEquals(actual, expected, message);
			}
		});
	}

	public void assertNotEquals(final long actual, final long expected, final String message) {
		doAssert(new SimpleAssert<Long>(actual, expected, message) {
			@Override
			public void doAssert() {
				com.apas.Assertions.CustomHardAssert.assertNotEquals(actual, expected, message);
			}
		});
	}

	public void assertNotEquals(final boolean actual, final boolean expected, final String message) {
		doAssert(new SimpleAssert<Boolean>(actual, expected, message) {
			@Override
			public void doAssert() {
				com.apas.Assertions.CustomHardAssert.assertNotEquals(actual, expected, message);
			}
		});
	}

	public void assertNotEquals(final byte actual, final byte expected, final String message) {
		doAssert(new SimpleAssert<Byte>(actual, expected, message) {
			@Override
			public void doAssert() {
				com.apas.Assertions.CustomHardAssert.assertNotEquals(actual, expected, message);
			}
		});
	}

	public void assertNotEquals(final char actual, final char expected, final String message) {
		doAssert(new SimpleAssert<Character>(actual, expected, message) {
			@Override
			public void doAssert() {
				com.apas.Assertions.CustomHardAssert.assertNotEquals(actual, expected, message);
			}
		});
	}

	public void assertNotEquals(final short actual, final short expected, final String message) {
		doAssert(new SimpleAssert<Short>(actual, expected, message) {
			@Override
			public void doAssert() {
				com.apas.Assertions.CustomHardAssert.assertNotEquals(actual, expected, message);
			}
		});
	}

	public void assertNotEquals(final int actual, final int expected, final String message) {
		doAssert(new SimpleAssert<Integer>(actual, expected, message) {
			@Override
			public void doAssert() {
				com.apas.Assertions.CustomHardAssert.assertNotEquals(actual, expected, message);
			}
		});
	}

	public void assertNotEquals(final float actual, final float expected, final float delta, final String message) {
		doAssert(new SimpleAssert<Float>(actual, expected, message) {
			@Override
			public void doAssert() {
				com.apas.Assertions.CustomHardAssert.assertNotEquals(actual, expected, delta, message);
			}
		});
	}

	public void assertNotEquals(final double actual, final double expected, final double delta, final String message) {
		doAssert(new SimpleAssert<Double>(actual, expected, message) {
			@Override
			public void doAssert() {
				com.apas.Assertions.CustomHardAssert.assertNotEquals(actual, expected, delta, message);
				//org.testng.Assert.assertNotEquals(actual, expected, delta, message);
			}
		});
	}
}
