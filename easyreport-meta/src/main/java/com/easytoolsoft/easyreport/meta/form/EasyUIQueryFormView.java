package com.easytoolsoft.easyreport.meta.form;

import com.easytoolsoft.easyreport.meta.form.control.HtmlCheckBox;
import com.easytoolsoft.easyreport.meta.form.control.HtmlCheckBoxList;
import com.easytoolsoft.easyreport.meta.form.control.HtmlComboBox;
import com.easytoolsoft.easyreport.meta.form.control.HtmlDateBox;
import com.easytoolsoft.easyreport.meta.form.control.HtmlSelectOption;
import com.easytoolsoft.easyreport.meta.form.control.HtmlTextBox;

/**
 * JQueryEasyUI控件报表查询参数表单视图
 *
 * @author Tom Deng
 * @date 2017-03-25
 */
public class EasyUIQueryFormView extends AbstractQueryParamFormView implements QueryParamFormView {
	@Override
	protected String getDateBoxText(final HtmlDateBox dateBox) {
		boolean disableIt = this.shouldMarkAsDisabled(dateBox);
		if (disableIt) {
			return "";
		}
		final String template = "<input id=\"%s\" name=\"%s\" type=\"text\" class=\"easyui-datebox\" required=\"true\" value=\"%s\" />";
		final String easyuiText = String.format(template, dateBox.getName(), dateBox.getName(), dateBox.getValue());
		return String.format("<span class=\"j-item\" ctrl-hidden=\"%s\" ctrl-type=\"%s\"><label style=\"width: 120px;\">%s:</label>%s</span>", dateBox.isHidden(), dateBox.getType(), dateBox.getText(), easyuiText);
	}

	@Override
	protected String getTexBoxText(final HtmlTextBox textBox) {
		boolean disableIt = this.shouldMarkAsDisabled(textBox);
		if (disableIt) {
			return "";
		}
		final String template = "<input id=\"%s\" name=\"%s\" type=\"text\" value=\"%s\" />";
		final String easyuiText = String.format(template, textBox.getName(), textBox.getName(), textBox.getValue());
		return String.format("<span class=\"j-item\" ctrl-hidden=\"%s\" ctrl-type=\"%s\"><label style=\"width: 120px;\">%s:</label>%s</span>", textBox.isHidden(), textBox.getType(), textBox.getText(), easyuiText);
	}

	@Override
	protected String getCheckBoxText(final HtmlCheckBox checkBox) {
		boolean disableIt = this.shouldMarkAsDisabled(checkBox);
		if (disableIt) {
			return "";
		}
		final String checked = checkBox.isChecked() ? "" : "checked=\"checked\"";
		return String.format("<span class=\"j-item\" ctrl-hidden=\"%s\" ctrl-type=\"%s\"><label><input id=\"%s\" name=\"%s\" type=\"checkbox\" value=\"%s\" %s />%s</label></span>", checkBox.isHidden(), checkBox.getType(),
				checkBox.getName(), checkBox.getName(), checkBox.getValue(), checked, checkBox.getText());
	}

	@Override
	protected String getComboBoxText(final HtmlComboBox comboBox) {
		boolean disableIt = this.shouldMarkAsDisabled(comboBox);
		if (disableIt) {
			return "";
		}
		final String multiple = comboBox.isMultipled() ? "data-options=\"multiple:true\"" : "";
		final StringBuilder htmlText = new StringBuilder("");
		htmlText.append(String.format("<span class=\"j-item\" ctrl-hidden=\"%s\" ctrl-type=\"%s\"><label style=\"width: 120px;\">%s:</label>", comboBox.isHidden(), comboBox.getType(), comboBox.getText()));
		htmlText.append(String.format("<select id=\"%s\" name=\"%s\" class=\"easyui-combobox\" editable=\"false\" style=\"width: 200px;\" %s >", comboBox.getName(), comboBox.getName(), multiple));
		for (final HtmlSelectOption option : comboBox.getValue()) {
			final String selected = option.isSelected() ? "selected=\"selected\"" : "";
			htmlText.append(String.format("<option value=\"%s\" %s>%s</option>", option.getValue(), selected, option.getText()));
		}
		htmlText.append("</select>");
		htmlText.append("</span>");
		return htmlText.toString();
	}

	@Override
	protected String getCheckboxListText(final HtmlCheckBoxList checkBoxList) {
		boolean isCheckedAll = true;
		final StringBuilder htmlText = new StringBuilder("");
		htmlText.append(String.format("<span class=\"j-item\" ctrl-type=\"%s\"><label style=\"width: 120px;\">%s:</label>", checkBoxList.getType(), checkBoxList.getText()));
		for (final HtmlCheckBox checkBox : checkBoxList.getValue()) {
			if (!checkBox.isChecked()) {
				isCheckedAll = false;
			}
			final String checked = checkBox.isChecked() ? "checked=\"checked\"" : "";
			htmlText.append(String.format("<label style=\"margin-right:4px;\"><input name=\"%s\" type=\"checkbox\" value=\"%s\" data-name=\"%s\" %s/>%s</label>", checkBoxList.getName(), checkBox.getName(), checkBox.getText(), checked,
					checkBox.getText()));
		}
		htmlText.append(String.format("<label><input id=\"checkAllStatColumn\" name=\"checkAllStatColumn\" type=\"checkbox\" %s />全选</label></span>", isCheckedAll ? "checked=\"checked\"" : ""));
		return htmlText.toString();
	}
}
