package org.sagebionetworks.web.client.widget.user;

import static org.sagebionetworks.web.client.DisplayUtils.DO_NOTHING_CLICKHANDLER;
import static org.sagebionetworks.web.client.DisplayUtils.newWindow;

import org.gwtbootstrap3.client.ui.constants.Emphasis;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.context.SynapseContextPropsProvider;
import org.sagebionetworks.web.client.jsni.SynapseContextProviderPropsJSNIObject;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.inject.Inject;

public class UserBadgeViewImpl extends Div implements UserBadgeView {
	private String userId;
	public static PlaceChanger placeChanger = null;
	public static final CallbackP<String> STANDARD_HANDLER = userId -> {
		placeChanger.goTo(new Profile(userId));
	};

	public static final CallbackP<String> NEW_WINDOW_HANDLER = userId -> {
		newWindow("#!Profile:" + userId, "_blank", "");
	};
	boolean showAvatar = false;
	boolean showCardOnHover = true;
	AdapterFactory adapterFactory;
	SynapseJSNIUtils jsniUtils;
	SynapseContextPropsProvider propsProvider;
	BadgeType badgeType = BadgeType.SMALL_CARD;
	AvatarSize avatarSize = AvatarSize.MEDIUM;
	CallbackP<String> currentClickHandler = STANDARD_HANDLER;
	FocusPanel userBadgeContainer = new FocusPanel();
	ReactComponentDiv userBadgeReactDiv = new ReactComponentDiv();
	JsArray<JavaScriptObject> menuActionsArray = null;
	AuthenticationController authController;
	boolean isReactHandlingClickEvents = false;

	@Inject
	public UserBadgeViewImpl(GlobalApplicationState globalAppState, SynapseJSNIUtils jsniUtils, AdapterFactory adapterFactory, AuthenticationController authController, final SynapseContextPropsProvider propsProvider) {
		placeChanger = globalAppState.getPlaceChanger();
		this.adapterFactory = adapterFactory;
		this.jsniUtils = jsniUtils;
		this.authController = authController;
		this.propsProvider = propsProvider;
		setMarginRight(2);
		setMarginLeft(2);
		addStyleName("UserBadge");
		addStyleName("vertical-align-middle");
		currentClickHandler = STANDARD_HANDLER;
		userBadgeContainer.addClickHandler(event -> {
			if (!isReactHandlingClickEvents) {
				event.stopPropagation();
				event.preventDefault();
				currentClickHandler.invoke(userId);
			}
		});
		userBadgeContainer.add(userBadgeReactDiv);
	}

	@Override
	public void configure(UserProfile profile, String pictureUrl, Boolean isCertified, Boolean isValidated) {
		userId = profile.getOwnerId();
		clear();
		add(userBadgeContainer);
		String profileJson = "";
		try {
			JSONObjectAdapter jsonObjectAdapter = adapterFactory.createNew();
			profile.writeToJSONObject(jsonObjectAdapter);
			profileJson = jsonObjectAdapter.toJSONString();
		} catch (Throwable e) {
			jsniUtils.consoleError(e);
		}
		_showBadge(userBadgeReactDiv.getElement(), profileJson, userId, badgeType.getUserCardType(), avatarSize.getAvatarSize(), showCardOnHover, pictureUrl, !authController.isLoggedIn(), showAvatar, isCertified, isValidated, menuActionsArray, this, propsProvider.getJsniContextProps());
	}

	public void setClickHandler(ClickHandler clickHandler) {
		currentClickHandler = userId -> {
			clickHandler.onClick(null);
		};
	}

	@Override
	public void setOpenInNewWindow() {
		currentClickHandler = NEW_WINDOW_HANDLER;
	}

	@Override
	public void setCustomClickHandler(final ClickHandler clickHandler) {
		setClickHandler(event -> {
			if (event != null) {
				event.stopPropagation();
				event.preventDefault();
			}
			clickHandler.onClick(event);
		});
		isReactHandlingClickEvents = false;
	}

	@Override
	public void doNothingOnClick() {
		setClickHandler(DO_NOTHING_CLICKHANDLER);
	}

	@Override
	public void setShowCardOnHover(boolean showCardOnHover) {
		this.showCardOnHover = showCardOnHover;
	}

	@Override
	public void setBadgeType(BadgeType badgeType) {
		this.badgeType = badgeType;
		switch (this.badgeType) {
			case SMALL_CARD:
				isReactHandlingClickEvents = true;
				removeStyleName("vertical-align-middle");
				addStyleName("inline-user-badge");
				break;
			case MEDIUM_CARD:
			case LARGE_CARD:
				isReactHandlingClickEvents = true;
				removeStyleName("vertical-align-middle");
				removeStyleName("inline-user-badge");
				break;
			case AVATAR:
				isReactHandlingClickEvents = false;
				addStyleName("vertical-align-middle");
				addStyleName("inline-user-badge");
				break;
		}
	}

	@Override
	public void setShowAvatar(boolean showAvatar) {
		this.showAvatar = showAvatar;
	}

	@Override
	public void setAvatarSize(AvatarSize avatarSize) {
		this.avatarSize = avatarSize;
	}

	@Override
	public void showLoadError(String error) {
		clear();
		Paragraph errorParagraph = new Paragraph();
		errorParagraph.setEmphasis(Emphasis.DANGER);
		errorParagraph.setText("Error loading profile: " + error);
		add(errorParagraph);
	}

	@Override
	public void showLoading() {
		clear();
		add(new Text("Loading..."));
	}

	@Override
	public void showInfo(String message) {
		// TODO Auto-generated method stub
	}

	@Override
	public void showErrorMessage(String message) {
		// TODO Auto-generated method stub
	}

	@Override
	public void addContextCommand(String commandName, Callback callback) {
		if (menuActionsArray == null) {
			menuActionsArray = _initJsArray();
		}
		// add to menu actions array
		_addToMenuActionsArray(commandName, callback, menuActionsArray);
	}

	private static native void _showBadge(Element el, String userProfileJson, String userId, String userCardType, String avatarSize, boolean showCardOnHover, String pictureUrl, boolean isEmailHidden, boolean showAvatar, Boolean isCertifiedUser, Boolean isValidatedProfile, JsArray<JavaScriptObject> menuActionsArray, UserBadgeViewImpl userBadgeView, SynapseContextProviderPropsJSNIObject wrapperProps) /*-{

		try {
			var userProfileObject = JSON.parse(userProfileJson);
			var userCardProps = {
				userProfile : userProfileObject,
				size : userCardType,
				avatarSize: avatarSize,
				showCardOnHover: showCardOnHover,
				menuActions : menuActionsArray,
				preSignedURL : pictureUrl,
				hideEmail : isEmailHidden,
				link : '#!Profile:' + userId,
				isCertified: isCertifiedUser,
				isValidated: isValidatedProfile,
				withAvatar: showAvatar
			};
			var component = $wnd.React.createElement($wnd.SRC.SynapseComponents.UserCard, userCardProps, null);
			var wrapper = $wnd.React.createElement($wnd.SRC.SynapseContext.SynapseContextProvider, wrapperProps, component);
			$wnd.ReactDOM.render(wrapper, el);
		} catch (err) {
			console.error(err);
		}
	}-*/;

	private static native JsArray<JavaScriptObject> _initJsArray() /*-{
		return [];
	}-*/;

	private static native void _addToMenuActionsArray(String commandName, Callback callback, JsArray<JavaScriptObject> menuActionsArray) /*-{
		function onMenuActionClick(userProfile) {
			callback.@org.sagebionetworks.web.client.utils.Callback::invoke()();
		}
		menuActionsArray.push({
			field : commandName,
			callback : onMenuActionClick
		});
	}-*/;
}
