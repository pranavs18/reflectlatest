package com.reflectmobile.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Spinner;
import android.widget.TextView;

import com.reflectmobile.R;
import com.reflectmobile.data.Community;
import com.reflectmobile.data.Network;
import com.reflectmobile.utility.NetworkManager.HttpGetImageTask;
import com.reflectmobile.utility.NetworkManager.HttpGetTask;
import com.reflectmobile.utility.NetworkManager.HttpImageTaskHandler;
import com.reflectmobile.utility.NetworkManager.HttpPostTask;
import com.reflectmobile.utility.NetworkManager.HttpTaskHandler;

public class CommunitiesActivity extends BaseActivity {

	private static String TAG = "CommunitiesActivity";

	private Community[] communities;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// It is important to set content view before calling super.onCreate
		// because BaseActivity uses references to side menu
		setContentView(R.layout.activity_communities);
		super.onCreate(savedInstanceState);

		// Modify action bar title
		int titleId = getResources().getIdentifier("action_bar_title", "id",
				"android");
		TextView title = (TextView) findViewById(titleId);
		title.setTextColor(getResources().getColor(R.color.yellow));
		title.setTypeface(Typeface.createFromAsset(getAssets(),
				"fonts/RobotoCondensed-Regular.ttf"));

		// Set margin before title
		ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) title
				.getLayoutParams();
		mlp.setMargins(5, 0, 0, 0);

		// Retreive data from the web
		final HttpTaskHandler getCommunitiesHandler = new HttpTaskHandler() {
			@Override
			public void taskSuccessful(String result) {
				// Parse JSON to the list of communities
				communities = Community.getCommunitiesInfo(result);
				GridView parentView = (GridView) findViewById(R.id.parentView);
				parentView
						.setAdapter(new CardAdapter(CommunitiesActivity.this));
			}

			@Override
			public void taskFailed(String reason) {
				Log.e(TAG, "Error within GET request: " + reason);
			}
		};

		new HttpGetTask(getCommunitiesHandler)
				.execute("http://rewyndr.truefitdemo.com/api/communities");

	}

	@Override
	public void onBackPressed() {
		signOut();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.communities_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action buttons
		switch (item.getItemId()) {
		case R.id.action_add_community:
			createCommunity();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public static class AddCommunityDialog extends DialogFragment {

		private boolean networkChosen = false;
		private boolean nameSet = false;
		private boolean descriptionSet = false;

		private int networkId;
		private String name;
		private String description;

		private Button saveButton;
		private Network[] mNetworks;

		public AddCommunityDialog() {

		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

			final View view = inflater.inflate(R.layout.add_community,
					container);
			Button cancelButton = (Button) view.findViewById(R.id.cancel);
			saveButton = (Button) view.findViewById(R.id.save);
			saveButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					saveButton.setEnabled(false);
					HttpTaskHandler httpPostTaskHandler = new HttpTaskHandler() {
						@Override
						public void taskSuccessful(String result) {
							Log.d("POST", result);
							dismiss();
							getActivity().finish();
							startActivity(getActivity().getIntent());
						}

						@Override
						public void taskFailed(String reason) {
							Log.e("POST", "Error within POST request: "
									+ reason);
						}
					};
					JSONObject communityData = new JSONObject();
					try {
						communityData.put("network_id", networkId);
						communityData.put("name", name);
						communityData.put("description", description);
					} catch (JSONException e) {
						Log.e(TAG, "Error forming JSON");
					}
					String payload = communityData.toString();
					new HttpPostTask(httpPostTaskHandler, payload)
							.execute("http://rewyndr.truefitdemo.com/api/communities?network_id="
									+ networkId);
				}
			});
			EditText communityName = (EditText) view
					.findViewById(R.id.community_name);
			EditText communityDesc = (EditText) view
					.findViewById(R.id.community_description);
			communityName.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
					nameSet = count > 0;
					modifySaveButton();
					if (nameSet) {
						name = s.toString();
					}
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
				}

				@Override
				public void afterTextChanged(Editable s) {
				}
			});

			communityDesc.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
					descriptionSet = count > 0;
					modifySaveButton();
					if (descriptionSet) {
						description = s.toString();
					}
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
				}

				@Override
				public void afterTextChanged(Editable s) {
				}
			});

			cancelButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
				}
			});

			final Spinner spinner = (Spinner) view.findViewById(R.id.network);

			String[] initialChoices = { "Choose a Network" };
			final ArrayAdapter<String> spinnerInitialAdapter = new ArrayAdapter<String>(
					getActivity(), R.layout.spinner, initialChoices);
			spinnerInitialAdapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(spinnerInitialAdapter);

			final HttpTaskHandler getNetworksHandler = new HttpTaskHandler() {
				@Override
				public void taskSuccessful(String result) {
					// Parse JSON to the list of networks
					mNetworks = Network.getNetworksInfo(result);
					String[] choices = new String[mNetworks.length + 1];
					choices[0] = "Choose a Network";
					for (int count = 0; count < mNetworks.length; count++) {
						choices[count + 1] = mNetworks[count].getName();
					}
					if (getActivity() != null) {
						final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
								getActivity(), R.layout.spinner, choices);
						spinnerArrayAdapter
								.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
						spinner.setAdapter(spinnerArrayAdapter);

						spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

							public void onItemSelected(AdapterView<?> parent,
									View view, int pos, long id) {
								networkChosen = pos > 0;
								modifySaveButton();
								if (networkChosen) {
									networkId = mNetworks[pos - 1].getId();
								}
							}

							public void onNothingSelected(AdapterView<?> parent) {

							}
						});
					}

				}

				@Override
				public void taskFailed(String reason) {
					Log.e(TAG, "Error within GET request: " + reason);
				}
			};
			new HttpGetTask(getNetworksHandler)
					.execute("http://rewyndr.truefitdemo.com/api/networks");
			return view;
		}

		private void modifySaveButton() {
			if (descriptionSet && nameSet && networkChosen) {
				saveButton.setTextColor(getResources().getColor(R.color.green));
				saveButton.setCompoundDrawablesWithIntrinsicBounds(
						R.drawable.tick_active, 0, 0, 0);
				saveButton.setEnabled(true);
			} else {
				saveButton.setTextColor(getResources().getColor(
						R.color.dark_gray));
				saveButton.setCompoundDrawablesWithIntrinsicBounds(
						R.drawable.tick_disabled, 0, 0, 0);
				saveButton.setEnabled(false);
			}
		}

	}

	public void createCommunity() {
		FragmentManager fm = getFragmentManager();
		AddCommunityDialog addCommunityDialog = new AddCommunityDialog();
		addCommunityDialog.show(fm, "fragment_add_community");
	}

	// Specific adapter for Communities Activity
	private class CardAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		private Context mContext;
		private Drawable[] mDrawables;

		public CardAdapter(Context context) {
			mDrawables = new Drawable[communities.length];
			mContext = context;
			mInflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			for (int count = 0; count < communities.length; count++) {
				final int index = count;

				// Load images asynchronously and notify about their loading
				new HttpGetImageTask(new HttpImageTaskHandler() {
					private int drawableIndex = index;

					@Override
					public void taskSuccessful(Drawable drawable) {
						mDrawables[drawableIndex] = drawable;
						notifyDataSetChanged();
					}

					@Override
					public void taskFailed(String reason) {
						Log.e(TAG, "Error downloading the image");
					}
				}).execute(communities[count].getFirstPhoto());
			}
		}

		@Override
		public int getCount() {
			return communities.length;
		}

		@Override
		public Object getItem(int item) {
			return item;
		}

		@Override
		public long getItemId(int id) {
			return id;
		}


		// Uses common Android ViewHolder pattern
		private class CardViewHolder {
			public View view;
			public ImageView image;
			public TextView text;
			public int position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parentView) {
			// If there is no view to recycle - create a new one
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.card, parentView,
						false);
				final CardViewHolder holder = new CardViewHolder();
				holder.view = convertView;
				holder.text = (TextView) convertView
						.findViewById(R.id.card_text);
				holder.image = (ImageView) convertView
						.findViewById(R.id.card_image);
				holder.image.setScaleType(ScaleType.CENTER_CROP);
				holder.view.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						int position = ((CardViewHolder) v.getTag()).position;
						Intent intent = new Intent(mContext,
								CommunityActivity.class);
						intent.putExtra("community_id",
								communities[position].getId());
						mContext.startActivity(intent);
					}
				});
				convertView.setTag(holder);
			}

			final CardViewHolder holder = (CardViewHolder) convertView.getTag();
			holder.position = position;

			holder.text.setText(communities[position].getName());
			holder.image.setImageDrawable(mDrawables[position]);

			return convertView;
		}
	}

}
