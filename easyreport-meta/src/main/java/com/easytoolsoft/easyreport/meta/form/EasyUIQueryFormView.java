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
		final String template = "<input id=\"%s\" name=\"%s\" type=\"text\" class=\"easyui-datebox\" required=\"true\" value=\"%s\" %s />";
		final String easyuiText = String.format(template, dateBox.getName(), dateBox.getName(), dateBox.getValue(), disableIt ? "disabled" : "");
		return String.format("<span class=\"j-item\"><label style=\"width: 120px;%s\">%s:</label>%s</span>&nbsp;", disableIt ? "color:gray;" : "", dateBox.getText(), easyuiText);
	}

	@Override
	protected String getTexBoxText(final HtmlTextBox textBox) {
		boolean disableIt = this.shouldMarkAsDisabled(textBox);
		final String template = "<input id=\"%s\" name=\"%s\" type=\"text\" value=\"%s\" %s />";
		final String easyuiText = String.format(template, textBox.getName(), textBox.getName(), textBox.getValue(), disableIt ? "disabled" : "");
		return String.format("<span class=\"j-item\"><label style=\"width: 120px;%s\">%s:</label>%s</span>&nbsp;", disableIt ? "color:gray;" : "", textBox.getText(), easyuiText);
	}

	@Override
	protected String getCheckBoxText(final HtmlCheckBox checkBox) {
		boolean disableIt = this.shouldMarkAsDisabled(checkBox);
		final String checked = checkBox.isChecked() ? "" : "checked=\"checked\"";
		return String.format("<input id=\"%s\" name=\"%s\" type=\"checkbox\" value=\"%s\" %s %s /><label>%s</label>&nbsp;", checkBox.getName(), checkBox.getName(), checkBox.getValue(), checked, disableIt ? "disabled" : "",
				checkBox.getText());
	}

	@Override
	protected String getComboBoxText(final HtmlComboBox comboBox) {
		boolean disableIt = this.shouldMarkAsDisabled(comboBox);
		final String multiple = comboBox.isMultipled() ? "data-options=\"multiple:true\"" : "";
		final StringBuilder htmlText = new StringBuilder("");
		htmlText.append(String.format("<span class=\"j-item\"><label style=\"width: 120px;%s\">%s:</label>", disableIt ? "color:gray;" : "", comboBox.getText()));
		htmlText.append(String.format("<select id=\"%s\" name=\"%s\" class=\"easyui-combobox\" style=\"width: 200px;\" %s %s >", comboBox.getName(), comboBox.getName(), multiple, disableIt ? "disabled" : ""));
		for (final HtmlSelectOption option : comboBox.getValue()) {
			final String selected = option.isSelected() ? "selected=\"selected\"" : "";
			htmlText.append(String.format("<option value=\"%s\" %s>%s</option>", option.getValue(), selected, option.getText()));
		}
		htmlText.append("</select>");
		htmlText.append("</span>&nbsp;");
		return htmlText.toString();
	}

	@Override
	protected String getCheckboxListText(final HtmlCheckBoxList checkBoxList) {
		boolean isCheckedAll = true;
		final StringBuilder htmlText = new StringBuilder("");
		htmlText.append(String.format("<span class=\"j-item\" data-type=\"checkbox\"><label style=\"width: 120px;\">%s:</label>", checkBoxList.getText()));
		for (final HtmlCheckBox checkBox : checkBoxList.getValue()) {
			if (!checkBox.isChecked()) {
				isCheckedAll = false;
			}
			final String checked = checkBox.isChecked() ? "checked=\"checked\"" : "";
			htmlText.append(String.format("<input name=\"%s\" type=\"checkbox\" value=\"%s\" data-name=\"%s\" %s/>%s &nbsp;", checkBoxList.getName(), checkBox.getName(), checkBox.getText(), checked, checkBox.getText()));
		}
		htmlText.append(String.format("<input id=\"checkAllStatColumn\" name=\"checkAllStatColumn\" type=\"checkbox\" %s />全选</span>", isCheckedAll ? "checked=\"checked\"" : ""));
		return htmlText.toString();
	}
}
