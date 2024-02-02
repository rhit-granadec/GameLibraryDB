Create OR ALTER Procedure getFrontPage(
	@Username varchar(20)
)
AS
	IF (@Username is null or not exists(SELECT Username from [User] where Username = @Username)) BEGIN
		Print('Username cannot be null or nonexsistant.')
		RETURN 1
	END

    select ga.[Name], ga.[Description], ga.[ReleaseDate], STRING_AGG(ge.[Name], ', ') AS 'Genres'
    from Game as ga
    Join GameHasGenre as ghg on ghg.GameID = ga.ID
    Join Genre as ge on ge.[Name] = ghg.GenreName
    Join UserHasGame as uhg on uhg.Game = ga.ID
    Join [User] as u on u.Username = uhg.[User]
	where u.Username = @Username
    Group by ga.[Name], ga.[Description], ga.[ReleaseDate]
GO
