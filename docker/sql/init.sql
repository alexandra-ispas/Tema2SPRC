CREATE TABLE tari (
    id SERIAL primary key,
    nume varchar(50) unique,
    lat double precision,
    lon double precision
);

CREATE TABLE IF NOT EXISTS orase (
    id SERIAL primary key,
    idTara int,
    nume varchar(50),
    lat double precision,
    lon double precision,
    constraint fk_idTara foreign key (idTara) references tari(id)
);

CREATE TABLE IF NOT EXISTS temperaturi (
    id SERIAL primary key,
    valoare double precision,
    timestamp varchar(50),
    idOras int,
    unique (idOras, timestamp),
    constraint fk_idOras foreign key (idOras) references orase(id)
);




