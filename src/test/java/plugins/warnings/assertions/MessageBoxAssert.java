package plugins.warnings.assertions;

import org.assertj.core.api.AbstractAssert;
import org.jenkinsci.test.acceptance.po.MessageBox;

public class MessageBoxAssert extends AbstractAssert<MessageBoxAssert, MessageBox> {

	public MessageBoxAssert(MessageBox actual) {
		super(actual, MessageBoxAssert.class);
	}

	public static MessageBoxAssert assertThat(MessageBox actual) {
		return new MessageBoxAssert(actual);
	}

	public MessageBoxAssert hasErrorMessagesSize(int size) {
		isNotNull();

		if (actual.getErrorMsgContent().size() != size) {
			failWithMessage("Expected size of error message box to be <%d> but was <%d>", size, actual.getErrorMsgContent().size());
		}

		return this;
	}

	public MessageBoxAssert hasInfoMessagesSize(int size) {
		isNotNull();

		if (actual.getInfoMsgContent().size() != size) {
			failWithMessage("Expected size of info message box to be <%d> but was <%d>", size, actual.getErrorMsgContent().size());
		}

		return this;
	}

	public MessageBoxAssert containsErrorMessage(String errorMessage) {
		isNotNull();

		for (String message : actual.getErrorMsgContent()) {
			if (message.contains(errorMessage)) {
				return this;
			}
		}

		failWithMessage("Expected message box to have error message <%s>", errorMessage);
		return this;
	}

	public MessageBoxAssert containsInfoMessage(String infoMessage) {
		isNotNull();

		for (String message : actual.getInfoMsgContent()) {
			if (message.contains(infoMessage)) {
				return this;
			}
		}

		// FIXME: show message box content
		failWithMessage("Expected message box to have info message <%s>", infoMessage);
		return this;
	}
}
