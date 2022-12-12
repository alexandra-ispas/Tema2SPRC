CREATE TABLE tari (
    id SERIAL primary key,
    nume varchar(50) unique,
    lat double precision,
    lon double precision,
    constraint upper_chk check (nume = INITCAP(nume))
);

CREATE TABLE IF NOT EXISTS orase (
    id SERIAL primary key,
    idTara int,
    nume varchar(50),
    lat double precision,
    lon double precision,
    unique (idTara, nume),
    constraint fk_idTara foreign key (idTara) references tari(id) ON DELETE CASCADE on update cascade,
    constraint upper_chk check (nume = INITCAP(nume))
);

CREATE TABLE IF NOT EXISTS temperaturi (
    id SERIAL primary key,
    valoare double precision,
    "timestamp" timestamp default CURRENT_TIMESTAMP,
    idOras int,
    unique (idOras, timestamp),
    constraint fk_idOras foreign key (idOras) references orase(id) ON DELETE CASCADE
);




