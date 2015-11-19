package com.github.alternet.demo.countries.client;

import com.github.alternet.demo.countries.client.model.Country;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.LabelProviderSafeHtmlRenderer;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.form.ComboBox;

/**
 * A sample GWT app that demonstrates how to use an autocompleter
 * that ignore diacritics and ligatures.
 *
 * @author Philippe Poulard
 */
public class Countries implements IsWidget, EntryPoint {

    interface CountryProperties extends PropertyAccess<Country> {
        ModelKeyProvider<Country> code();
        LabelProvider<Country> name();

        CountryProperties INSTANCE = GWT.create(CountryProperties.class);
    }

    public void onModuleLoad() {
        RootPanel.get().add(this);
    }

    public Widget asWidget() {
        ListStore<Country> countries = loadCountries();

        ComboBox<Country> autocompleter = new ComboBox<Country>(
            new ComboBoxCell<Country>(
                countries,
                CountryProperties.INSTANCE.name(),
                new LabelProviderSafeHtmlRenderer<Country>(CountryProperties.INSTANCE.name())
            ) {
                @Override
                protected boolean itemMatchesQuery(Country item, String query) {
                    String value = getPropertyEditor().render(item);
                    if (value != null) {
                        String latinValue = LatinMapper.latinize(value).toLowerCase().replace('-', ' ');
                        String latinQuery = LatinMapper.latinize(query).toLowerCase().replace('-', ' ');
                        return latinValue.startsWith(latinQuery);
                    }
                    return false;
                  }
            }
        );
        autocompleter.setEmptyText("Select a country...");
        autocompleter.setWidth(150);
        autocompleter.setTypeAhead(true);
        autocompleter.setTriggerAction(TriggerAction.ALL);

        VerticalPanel panel = new VerticalPanel();
        panel.add(autocompleter);
        return panel;
    }

    public ListStore<Country> loadCountries() {
        final ListStore<Country> countries = new ListStore<Country> (CountryProperties.INSTANCE.code());
        String path = "country.json";
        try {
            RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, path);
            builder.sendRequest(null, new RequestCallback() {
                public void onError(Request request, Throwable exception) {
                    GWT.log("Couldn't retrieve JSON");
                }
                public void onResponseReceived(Request request, Response response) {
                    if (200 == response.getStatusCode()) {
                        JSONValue val = JSONParser.parseLenient(response.getText());
                        for (String code : val.isObject().keySet()) {
                            countries.add(new Country(code, val.isObject().get(code).isString().stringValue()));
                        }
                    } else {
                        GWT.log("Couldn't retrieve JSON (" + response.getStatusText()
                        + ")");
                    }
                }
            });
        } catch (RequestException e) {
            GWT.log("Couldn't retrieve JSON");
        }
        return countries;
    }

}
