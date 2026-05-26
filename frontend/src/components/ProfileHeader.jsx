import Button from './Button.jsx'
import Avatar from './Avatar.jsx'

function ProfileHeader({
                           user,
                           metrics = {},
                           isOwnProfile = true,
                           isFollowing = false,
                           onToggleFollow,
                           onEdit,
                           onMessage,
                       }) {
    const collectionAverageRating = metrics.collectionAverageRating || '—'
    const totalItemLikes = metrics.totalItemLikes || 0
    const followersCount = metrics.followersCount || 0
    const totalCollectionsValueLabel = metrics.totalCollectionsValueLabel || '0 ₽'

    return (
        <section className="profile-hero">
            <Avatar tone={user.avatarTone} size="lg" label={user.name}/>
            <div className="profile-hero__info">
                <div className="profile-hero__top">
                    <h1>{user.name}</h1>

                    <div className="profile-hero__actions">
                        {!isOwnProfile && (
                            <>
                                <Button variant="secondary" onClick={onMessage}>Сообщения</Button>
                                <Button
                                    variant={isFollowing ? 'ghost' : 'primary'}
                                    className="profile-hero__follow"
                                    onClick={onToggleFollow}
                                >
                                    {isFollowing ? 'Вы подписаны' : 'Подписаться'}
                                </Button>
                            </>
                        )}

                        {isOwnProfile && onEdit && (
                            <Button variant="ghost" onClick={onEdit}>Редактировать</Button>
                        )}
                    </div>
                </div>

                <div className="profile-hero__identity">
                    <p className="profile-hero__handle">{user.handle}</p>
                    <p className="profile-hero__status">{user.status}</p>
                </div>

                <p>{user.about}</p>

                <div className="profile-hero__meta" aria-label="Показатели профиля">
          <span title="Средняя оценка коллекций" aria-label="Средняя оценка коллекций">
            ★ {collectionAverageRating}
          </span>
                    <span>{totalItemLikes} лайков на предметах</span>
                    <span>{followersCount} подписчиков</span>
                    <span>{totalCollectionsValueLabel} в коллекциях</span>
                </div>
            </div>
        </section>
    )
}

export default ProfileHeader