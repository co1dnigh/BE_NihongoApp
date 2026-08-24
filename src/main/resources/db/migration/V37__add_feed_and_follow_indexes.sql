-- Bang posts/post_likes/post_comments (V5) va user_follows (V8) da co san — chi bo sung
-- index can cho module Social Feed (feed + danh sach followers/following).

-- user_follows da co idx_followed (tren followed_id) tu V8, con thieu index tren follower_id
-- (dung khi tra "toi dang follow ai" va khi query danh sach following cua 1 user).
CREATE INDEX idx_user_follows_follower ON user_follows(follower_id);

-- Ho tro truc tiep query feed: WHERE user_id IN (...) AND deleted_at IS NULL ORDER BY
-- created_at DESC, id DESC.
CREATE INDEX idx_posts_feed ON posts(user_id, created_at DESC, id DESC);
