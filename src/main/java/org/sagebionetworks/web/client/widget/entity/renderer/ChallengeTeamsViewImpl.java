package org.sagebionetworks.web.client.widget.entity.renderer;


import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.team.TeamBadge;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * View that contains a list of challenge teams
 * @author jayhodgson
 *
 */
public class ChallengeTeamsViewImpl implements ChallengeTeamsView {
	
	public interface Binder extends	UiBinder<Widget, ChallengeTeamsViewImpl> {}

	private Presenter presenter;
	PortalGinInjector ginInjector;
	
	@UiField
	Div paginationWidgetContainer;
	@UiField
	Div dialogWidgetContainer;
	@UiField
	Div challengeTeamsContainer;
	
	@UiField
	Div loadingUI;
	
	@UiField
	Alert errorUI;
	@UiField
	Text errorText;
	
	Widget widget;
	
	@Inject
	public ChallengeTeamsViewImpl(Binder binder, PortalGinInjector ginInjector) {
		widget = binder.createAndBindUi(this);
		this.ginInjector = ginInjector;
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}
	@Override
	public void clearTeams() {
		challengeTeamsContainer.clear();
	}
	
	@Override
	public void addChallengeTeam(final String teamId, final String message, boolean showEditButton) {
		Div div = new Div();
		TeamBadge newTeamBadge = ginInjector.getTeamBadgeWidget();
		newTeamBadge.configure(teamId);
		Span messageSpan = new Span();
		messageSpan.addStyleName("greyText-imp");
		messageSpan.setText(DisplayUtils.replaceWithEmptyStringIfNull(message));
		
		div.add(newTeamBadge.asWidget());
		div.add(messageSpan.asWidget());
		if (showEditButton) {
			Button editButton = new Button("", IconType.EDIT, new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					presenter.onEdit(teamId, message);
				}
			});
			div.add(editButton);
		}
	}
	
	@Override
	public void setEditRegisteredTeamDialog(Widget dialogWidget) {
		dialogWidgetContainer.clear();
		dialogWidgetContainer.add(dialogWidget);
	}
	@Override
	public void setPaginationWidget(Widget paginationWidget) {
		paginationWidgetContainer.clear();
		paginationWidgetContainer.add(paginationWidget);
	}
	
	@Override
	public void clear() {
		clearTeams();
		hideErrors();
	}
	
	@Override
	public void showErrorMessage(String message) {
		errorUI.setText(message);
		errorUI.setVisible(true);
	}
	@Override
	public void hideErrors() {
		errorUI.setVisible(false);
	}
	
	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}
	
	@Override
	public void showLoading() {
		loadingUI.setVisible(true);
	}
	@Override
	public void hideLoading() {
		loadingUI.setVisible(false);
	}
}
