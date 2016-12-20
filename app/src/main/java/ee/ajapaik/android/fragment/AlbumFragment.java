package ee.ajapaik.android.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import ee.ajapaik.android.AlbumsActivity;
import ee.ajapaik.android.PhotoActivity;
import ee.ajapaik.android.ProfileActivity;
import ee.ajapaik.android.adapter.PhotoAdapter;
import ee.ajapaik.android.data.Album;
import ee.ajapaik.android.data.Photo;
import ee.ajapaik.android.data.util.Status;
import ee.ajapaik.android.fragment.util.WebFragment;
import ee.ajapaik.android.test.R;
import ee.ajapaik.android.util.Objects;
import ee.ajapaik.android.util.WebAction;
import ee.ajapaik.android.widget.StaggeredGridView;

public class AlbumFragment extends WebFragment {
    private static final String KEY_ALBUM_IDENTIFIER = "album_id";

    private static final String KEY_ALBUM = "album";
    private static final String KEY_LAYOUT = "layout";

    private Album m_album;

    public void invalidate(boolean force) {
        if(m_album == null || force) {
            onRefresh(false);
        }
    }

    public String getAlbumIdentifier() {
        Bundle arguments = getArguments();

        return (arguments != null) ? arguments.getString(KEY_ALBUM_IDENTIFIER) : null;
    }

    public void setAlbumIdentifier(String albumIdentifier) {
        Bundle arguments = getArguments();

        if(arguments == null) {
            arguments = new Bundle();
        }

        if(albumIdentifier != null) {
            arguments.putString(KEY_ALBUM_IDENTIFIER, albumIdentifier);
        } else {
            arguments.remove(KEY_ALBUM_IDENTIFIER);
        }

        setArguments(arguments);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_album, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState != null) {
            Album album = savedInstanceState.getParcelable(KEY_ALBUM);
            Parcelable layout = savedInstanceState.getParcelable(KEY_LAYOUT);

            setAlbum(album, layout);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_refresh) {
            onRefresh(true);

            return true;
        } else if(id == R.id.action_profile) {
            ProfileActivity.start(getActivity());

            return true;
        } else if(id == R.id.action_albums) {
            AlbumsActivity.start(getActivity());

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putParcelable(KEY_ALBUM, m_album);
    }

    @Override
    public void onStart() {
        super.onStart();
        onRefresh(false);
    }

    public Album getAlbum() {
        return m_album;
    }

    public void setAlbum(Album album) {
        setAlbum(album, null);
    }

    public void setAlbum(Album album, Parcelable state) {
        if(!Objects.match(m_album, album)) {
            StaggeredGridView gridView = getGridView();

            m_album = album;

            if(m_album != null && m_album.getTitle() != null) {
                getActionBar().setTitle(m_album.getTitle());
            }

            if(state == null) {
                state = gridView.getLayoutManager().onSaveInstanceState();
            }

            if(m_album != null && m_album.getPhotos().size() > 0) {
                getEmptyView().setText("");
                gridView.setAdapter(new PhotoAdapter(gridView.getContext(), m_album.getPhotos(), getSettings().getLocation(), new PhotoAdapter.OnPhotoSelectionListener() {
                    @Override
                    public void onSelect(Photo photo) {
                        PhotoActivity.start(getActivity(), photo, m_album);
                    }
                }));
            } else {
                getEmptyView().setText(getPlaceholderString());
                gridView.setAdapter(null);
            }

            if(state != null) {
                gridView.getLayoutManager().onRestoreInstanceState(state);
            }
        }
    }

    protected void onRefresh(final boolean animated) {
        Context context = getActivity();
        WebAction<Album> action = createAction(context);

        if(m_album == null) {
            getProgressBar().setVisibility(View.VISIBLE);
        }

        if(action != null) {
            getConnection().enqueue(context, action, new WebAction.ResultHandler<Album>() {
                @Override
                public void onActionResult(Status status, Album album) {
                    if(m_album == null) {
                        getProgressBar().setVisibility(View.GONE);
                    }

                    if(album != null) {
                        setAlbum(album);
                    } else if(m_album == null || animated) {
                        // TODO: Show error alert
                    }
                }
            });
        }
    }

    protected String getPlaceholderString() {
        return getString(R.string.albums_label_no_data);
    }

    protected WebAction<Album> createAction(Context context) {
        return (m_album != null) ? Album.createStateAction(context, m_album) : Album.createStateAction(context, getAlbumIdentifier());
    }

    private TextView getEmptyView() {
        return (TextView)getView().findViewById(R.id.empty);
    }

    private StaggeredGridView getGridView() {
        return getGridView(getView());
    }

    private StaggeredGridView getGridView(View view) {
        return (StaggeredGridView)view.findViewById(R.id.grid);
    }

    private ProgressBar getProgressBar() {
        return (ProgressBar)getView().findViewById(R.id.progress_bar);
    }
}