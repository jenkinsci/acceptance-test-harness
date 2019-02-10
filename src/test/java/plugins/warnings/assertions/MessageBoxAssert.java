package plugins.warnings.assertions;

import org.assertj.core.api.AbstractAssert;
import org.jenkinsci.test.acceptance.plugins.warnings_ng.LogMessagesView;

public class MessageBoxAssert extends AbstractAssert<MessageBoxAssert, LogMessagesView> {

	public MessageBoxAssert(LogMessagesView actual) {
		super(actual, MessageBoxAssert.class);
	}

	public static MessageBoxAssert assertThat(LogMessagesView actual) {
		return new MessageBoxAssert(actual);
	}

	public MessageBoxAssert hasErrorMessagesSize(int size) {
		isNotNull();

		if (actual.getErrorMessages().size() != size) {
			failWithMessage("Expected size of error message box to be <%d> but was <%d>", size, actual.getErrorMessages().size());
		}

		return this;
	}

	public MessageBoxAssert hasInfoMessagesSize(int size) {
		isNotNull();

		if (actual.getInfoMessages().size() != size) {
			failWithMessage("Expected size of info message box to be <%d> but was <%d>", size, actual.getErrorMessages().size());
		}

		return this;
	}

	public MessageBoxAssert containsErrorMessage(String errorMessage) {
		isNotNull();

		for (String message : actual.getErrorMessages()) {
			if (message.contains(errorMessage)) {
				return this;
			}
		}

		failWithMessage("Expected message box to have error message <%s>", errorMessage);
		return this;
	}

	public MessageBoxAssert containsInfoMessage(String infoMessage) {
		isNotNull();

		for (String message : actual.getInfoMessages()) {
			if (message.contains(infoMessage)) {
				return this;
			}
		}

		// FIXME: show message box content
		failWithMessage("Expected message box to have info message <%s>", infoMessage);
		return this;
	}
}
